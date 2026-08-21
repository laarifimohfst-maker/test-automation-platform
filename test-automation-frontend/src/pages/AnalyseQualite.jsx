import { useEffect, useMemo, useState } from 'react';
import {
  Play,
  ShieldCheck,
  Bug,
  ShieldAlert,
  Code2,
  PieChart,
  Copy,
  Trash2,
  Download,
} from 'lucide-react';

import { obtenirProjetsUtilisateur } from '../services/projetService';

import {
  creerExecutionAnalyse,
  executerAnalyse,
  obtenirExecutionsAnalyseParProjet,
  obtenirAnalyseParExecution,
  supprimerAnalyseQualite,
  supprimerExecutionAnalyse,
} from '../services/analyseQualiteService';

import {
  genererRapportAnalyseQualite,
  telechargerRapport,
  enregistrerFichierRapport,
} from '../services/rapportService';

import './AnalyseQualite.css';
import { useAlertDialog } from '../components/AlertDialogContext';

const UTILISATEUR_ID = 1;

function AnalyseQualite() {
  const { demanderConfirmation } = useAlertDialog();

  const [projets, setProjets] = useState([]);
  const [projetSelectionneId, setProjetSelectionneId] = useState('');
  const [executions, setExecutions] = useState([]);
  const [analyses, setAnalyses] = useState({});

  const [chargementProjets, setChargementProjets] = useState(true);
  const [chargementHistorique, setChargementHistorique] = useState(false);
  const [analyseEnCours, setAnalyseEnCours] = useState(false);

  const [suppressionId, setSuppressionId] = useState(null);
  const [rapportEnCoursId, setRapportEnCoursId] = useState(null);

  const [erreur, setErreur] = useState(null);
  const [succes, setSucces] = useState(null);
  /*
   * =============================
   * Chargement des projets
   * =============================
   */
  useEffect(() => {
    setChargementProjets(true);

    obtenirProjetsUtilisateur(UTILISATEUR_ID)
      .then((res) => {
        setProjets(res.data);

        if (res.data.length > 0) {
          setProjetSelectionneId(String(res.data[0].id));
        }
      })
      .catch((err) => {
        console.error(err);
        setErreur('Impossible de charger les projets.');
      })
      .finally(() => {
        setChargementProjets(false);
      });
  }, []);

  /*
   * =============================
   * Projet sélectionné
   * =============================
   */
  const projetSelectionne = useMemo(() => {
    return projets.find(
      (p) => String(p.id) === String(projetSelectionneId)
    );
  }, [projets, projetSelectionneId]);

  /*
   * =============================
   * Chargement des analyses
   * =============================
   */
  const chargerHistorique = async (projetId) => {
    if (!projetId) {
      setExecutions([]);
      setAnalyses({});
      return;
    }

    setChargementHistorique(true);

    try {
      const response = await obtenirExecutionsAnalyseParProjet(projetId);

      const executionsRecues = [...response.data].sort(
        (a, b) =>
          new Date(b.dateDebut || 0) -
          new Date(a.dateDebut || 0)
      );

      setExecutions(executionsRecues);

      const analysesChargees = {};

      /*
       * Une analyse peut exister pour :
       *
       * TERMINEE :
       * Quality Gate réussi
       *
       * ECHOUEE :
       * Quality Gate échoué
       *
       * Certaines anciennes exécutions échouées
       * peuvent ne pas avoir d'AnalyseQualite.
       */
      for (const execution of executionsRecues) {
        if (
          execution.statut !== 'TERMINEE' &&
          execution.statut !== 'ECHOUEE'
        ) {
          continue;
        }

        try {
          const analyseResponse = await obtenirAnalyseParExecution(
            execution.id
          );

          analysesChargees[execution.id] = analyseResponse.data;
        } catch (err) {
          console.log(
            `Pas d'analyse pour l'exécution ${execution.id}`
          );
        }
      }

      setAnalyses(analysesChargees);
    } catch (err) {
      console.error(err);
      setErreur('Impossible de charger les analyses.');
    } finally {
      setChargementHistorique(false);
    }
  };

  useEffect(() => {
    if (projetSelectionneId) {
      setErreur(null);
      setSucces(null);
      chargerHistorique(projetSelectionneId);
    }
  }, [projetSelectionneId]);

  /*
   * =============================
   * Lancer une analyse
   * =============================
   */
  const gererAnalyse = async () => {
    if (!projetSelectionneId) {
      setErreur('Sélectionne un projet.');
      return;
    }

    setAnalyseEnCours(true);
    setErreur(null);
    setSucces(null);

    try {
      /*
       * Étape 1 :
       * création ExecutionAnalyseQualite
       */
      const creation = await creerExecutionAnalyse(
        projetSelectionneId
      );

      const executionId = creation.data.id;

      /*
       * Étape 2 :
       * analyse SonarQube
       */
      const resultat = await executerAnalyse(executionId);

      /*
       * Ton backend peut retourner HTTP 200
       * même avec statut ECHOUEE.
       */
      if (
        resultat.data.statut === 'ECHOUEE' &&
        resultat.data.message?.startsWith(
          "Erreur lors de l'analyse"
        )
      ) {
        setErreur(resultat.data.message);
      } else {
        setSucces('Analyse de qualité terminée.');
      }

      /*
       * Recharge les analyses
       */
      await chargerHistorique(projetSelectionneId);
    } catch (err) {
      console.error("Erreur pendant l'analyse", err);

      setErreur(
        "Impossible d'exécuter l'analyse de qualité."
      );
    } finally {
      setAnalyseEnCours(false);
    }
  };

  /*
   * =============================
   * Supprimer une analyse
   * =============================
   */
  const gererSuppression = async (execution) => {
    const confirmation = await demanderConfirmation({
      titre: 'Supprimer cette analyse ?',
      message:
        'Les métriques et les données de qualité associées seront supprimées.',
      texteConfirmation: 'Supprimer',
    });

    if (!confirmation) {
      return;
    }

    setSuppressionId(execution.id);
    setErreur(null);
    setSucces(null);

    try {
      /*
       * Récupère AnalyseQualite associée
       */
      const analyse = analyses[execution.id];

      /*
       * 1. Si une AnalyseQualite existe,
       * on la supprime d'abord.
       */
      if (analyse?.id) {
        await supprimerAnalyseQualite(analyse.id);
      }

      /*
       * 2. Ensuite on supprime
       * ExecutionAnalyseQualite.
       */
      await supprimerExecutionAnalyse(execution.id);

      setSucces('Analyse supprimée avec succès.');

      /*
       * 3. Recharge le tableau
       */
      await chargerHistorique(projetSelectionneId);
    } catch (err) {
      console.error(
        "Erreur lors de la suppression :",
        err
      );

      console.error(
        "Réponse backend :",
        err.response?.data
      );

      setErreur(
        "Impossible de supprimer l'analyse."
      );
    } finally {
      setSuppressionId(null);
    }
  };

  /*
   * =============================
   * Générer un rapport
   * =============================
   */
  const gererGenerationRapport = async (execution) => {
    setRapportEnCoursId(execution.id);
    setErreur(null);
    setSucces(null);

    try {
      const generation = await genererRapportAnalyseQualite(
        execution.id
      );

      const rapport = generation.data;

      const telechargement = await telechargerRapport(
        rapport.id
      );

      enregistrerFichierRapport(
        telechargement.data,
        rapport.nom ||
          `rapport_qualite_execution_${execution.id}`
      );

      setSucces(
        'Le rapport PDF de l’analyse qualité a été généré et téléchargé.'
      );
    } catch (err) {
      console.error(
        'Erreur lors de la génération du rapport qualité :',
        err
      );

      setErreur(
        err.response?.data?.message ||
          "Impossible de générer le rapport de cette analyse."
      );
    } finally {
      setRapportEnCoursId(null);
    }
  };

  /*
   * =============================
   * Dernière analyse disponible
   * =============================
   */
  const derniereExecutionAnalyse = useMemo(() => {
    return executions.find(
      (execution) => analyses[execution.id]
    );
  }, [executions, analyses]);

  const derniereAnalyse = derniereExecutionAnalyse
    ? analyses[derniereExecutionAnalyse.id]
    : null;

  /*
   * =============================
   * Utilitaires
   * =============================
   */
  const formaterDate = (date) => {
    if (!date) {
      return '—';
    }

    return new Date(date).toLocaleString('fr-FR');
  };

  const formaterDuree = (debut, fin) => {
    if (!debut || !fin) {
      return '—';
    }

    const difference =
      new Date(fin) -
      new Date(debut);

    if (difference < 0) {
      return '—';
    }

    const secondes = Math.floor(difference / 1000);
    const minutes = Math.floor(secondes / 60);
    const reste = secondes % 60;

    if (minutes === 0) {
      return `${reste} s`;
    }

    return `${minutes} min ${reste} s`;
  };

  const libelleQualityGate = (statut) => {
    if (statut === 'REUSSI') {
      return 'Réussi';
    }

    if (statut === 'ECHOUE') {
      return 'Échoué';
    }

    return '—';
  };

  return (
    <div className="aq-page">

      {/* ===================================
          LANCER UNE ANALYSE
      =================================== */}

      <div className="aq-card">
        <h3 className="aq-card-title">
          Lancer une analyse
        </h3>

        <div className="aq-launch-grid">
          <div>
            <label className="aq-label">
              Choisir un projet
            </label>

            <select
              className="aq-select"
              value={projetSelectionneId}
              disabled={chargementProjets}
              onChange={(e) =>
                setProjetSelectionneId(e.target.value)
              }
            >
              <option value="">
                -- Sélectionne un projet --
              </option>

              {projets.map((projet) => (
                <option
                  key={projet.id}
                  value={projet.id}
                >
                  {projet.nom}
                </option>
              ))}
            </select>

            {projetSelectionne && (
              <div className="aq-project-info">
                <div className="aq-project-avatar">
                  {projetSelectionne.nom
                    ?.charAt(0)
                    .toUpperCase()}
                </div>

                <div>
                  <strong>
                    {projetSelectionne.nom}
                  </strong>

                  <span>
                    Source :{' '}
                    {projetSelectionne.typeSource ===
                    'GITHUB'
                      ? 'GitHub'
                      : 'Archive ZIP'}
                  </span>
                </div>
              </div>
            )}
          </div>

          <div className="aq-launch-action">
            <button
              className="aq-launch-button"
              onClick={gererAnalyse}
              disabled={
                analyseEnCours ||
                !projetSelectionneId
              }
            >
              <Play size={17} />

              {analyseEnCours
                ? 'Analyse en cours...'
                : "Lancer l'analyse"}
            </button>

            <span>
              L'analyse peut prendre quelques minutes.
            </span>
          </div>
        </div>

        {erreur && (
          <div className="aq-error">
            {erreur}
          </div>
        )}

        {succes && (
          <div className="aq-success">
            {succes}
          </div>
        )}
      </div>

      {/* ===================================
          MÉTRIQUES DERNIÈRE ANALYSE
      =================================== */}

      {derniereAnalyse && (
        <>
          <div className="aq-metrics">

            {/* Quality Gate */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Quality Gate</span>

                <ShieldCheck
                  size={27}
                  className="aq-green"
                />
              </div>

              <strong
                className={
                  derniereAnalyse.qualityGateStatus ===
                  'REUSSI'
                    ? 'aq-value-green'
                    : 'aq-value-red'
                }
              >
                {libelleQualityGate(
                  derniereAnalyse.qualityGateStatus
                )}
              </strong>
            </div>

            {/* Bugs */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Bugs</span>

                <Bug
                  size={27}
                  className="aq-red"
                />
              </div>

              <strong className="aq-value-red">
                {derniereAnalyse.bugs ?? 0}
              </strong>
            </div>

            {/* Vulnérabilités */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Vulnérabilités</span>

                <ShieldAlert
                  size={27}
                  className="aq-orange"
                />
              </div>

              <strong className="aq-value-orange">
                {derniereAnalyse.vulnerabilites ?? 0}
              </strong>
            </div>

            {/* Code Smells */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Code Smells</span>

                <Code2
                  size={27}
                  className="aq-purple"
                />
              </div>

              <strong className="aq-value-purple">
                {derniereAnalyse.codeSmells ?? 0}
              </strong>
            </div>

            {/* Couverture */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Couverture</span>

                <PieChart
                  size={27}
                  className="aq-blue"
                />
              </div>

              <strong className="aq-value-blue">
                {Number(
                  derniereAnalyse.coverage ?? 0
                ).toFixed(1)}
                %
              </strong>
            </div>

            {/* Duplication */}
            <div className="aq-metric-card">
              <div className="aq-metric-header">
                <span>Duplication</span>

                <Copy
                  size={27}
                  className="aq-orange"
                />
              </div>

              <strong className="aq-value-orange">
                {Number(
                  derniereAnalyse.duplication ?? 0
                ).toFixed(1)}
                %
              </strong>
            </div>
          </div>

          <div className="aq-last-analysis">
            <span>Dernière analyse :</span>

            <strong>
              {formaterDate(
                derniereAnalyse.dateAnalyse
              )}
            </strong>
          </div>
        </>
      )}

      {/* ===================================
          DERNIÈRES ANALYSES
      =================================== */}

      <div className="aq-card">
        <h3 className="aq-card-title">
          Dernières analyses
        </h3>

        {chargementHistorique ? (
          <div className="aq-empty">
            Chargement...
          </div>
        ) : executions.length === 0 ? (
          <div className="aq-empty">
            Aucune analyse pour ce projet.
          </div>
        ) : (
          <div className="aq-table-wrapper">
            <table className="aq-table">

              <thead>
                <tr>
                  <th>Projet</th>
                  <th>Quality Gate</th>
                  <th>Bugs</th>
                  <th>Vulnérabilités</th>
                  <th>Code Smells</th>
                  <th>Couverture</th>
                  <th>Duplication</th>
                  <th>Date d'analyse</th>
                  <th>Durée</th>

                  <th className="aq-action-column">
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody>
                {executions.map((execution) => {
                  const analyse =
                    analyses[execution.id];

                  return (
                    <tr key={execution.id}>

                      {/* Projet */}
                      <td>
                        {projetSelectionne?.nom || '—'}
                      </td>

                      {/* Quality Gate */}
                      <td>
                        {analyse ? (
                          <span
                            className={`aq-badge ${
                              analyse.qualityGateStatus ===
                              'REUSSI'
                                ? 'aq-badge-success'
                                : 'aq-badge-error'
                            }`}
                          >
                            {libelleQualityGate(
                              analyse.qualityGateStatus
                            )}
                          </span>
                        ) : (
                          <span
                            className={`aq-badge ${
                              execution.statut ===
                              'EN_COURS'
                                ? 'aq-badge-warning'
                                : 'aq-badge-neutral'
                            }`}
                          >
                            {execution.statut === 'EN_COURS'
                              ? 'En cours'
                              : execution.statut === 'EN_ATTENTE'
                              ? 'En attente'
                              : execution.statut === 'ECHOUEE'
                              ? 'Échouée'
                              : 'Indisponible'}
                          </span>
                        )}
                      </td>

                      {/* Bugs */}
                      <td>
                        {analyse?.bugs ?? '—'}
                      </td>

                      {/* Vulnérabilités */}
                      <td>
                        {analyse?.vulnerabilites ?? '—'}
                      </td>

                      {/* Code Smells */}
                      <td>
                        {analyse?.codeSmells ?? '—'}
                      </td>

                      {/* Couverture */}
                      <td>
                        {analyse?.coverage != null
                          ? `${Number(
                              analyse.coverage
                            ).toFixed(1)}%`
                          : '—'}
                      </td>

                      {/* Duplication */}
                      <td>
                        {analyse?.duplication != null
                          ? `${Number(
                              analyse.duplication
                            ).toFixed(1)}%`
                          : '—'}
                      </td>

                      {/* Date */}
                      <td>
                        {formaterDate(
                          analyse?.dateAnalyse
                        )}
                      </td>

                      {/* Durée */}
                      <td>
                        {formaterDuree(
                          execution.dateDebut,
                          execution.dateFin
                        )}
                      </td>

                      {/* Actions */}
                      <td className="aq-action-column">
                        <div className="aq-action-buttons">

                          <button
                            type="button"
                            className="aq-report-button"
                            title="Générer et télécharger le rapport PDF"
                            aria-label={`Générer le rapport PDF de l'exécution ${execution.id}`}
                            disabled={
                              !analyse ||
                              rapportEnCoursId !== null
                            }
                            onClick={() =>
                              gererGenerationRapport(
                                execution
                              )
                            }
                          >
                            <Download size={17} />

                            <span>
                              {rapportEnCoursId === execution.id
                                ? 'Génération...'
                                : 'Rapport'}
                            </span>
                          </button>

                          <button
                            type="button"
                            className="aq-delete-button"
                            title="Supprimer l'analyse"
                            aria-label={`Supprimer l'analyse de l'exécution ${execution.id}`}
                            disabled={
                              suppressionId === execution.id ||
                              rapportEnCoursId === execution.id
                            }
                            onClick={() =>
                              gererSuppression(
                                execution
                              )
                            }
                          >
                            <Trash2 size={17} />
                          </button>

                        </div>
                      </td>

                    </tr>
                  );
                })}
              </tbody>

            </table>
          </div>
        )}
      </div>

    </div>
  );
}

export default AnalyseQualite;
