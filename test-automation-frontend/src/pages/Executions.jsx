import { useEffect, useMemo, useState } from 'react';
import { Eye, Play, Trash2 } from 'lucide-react';
import { obtenirProjetsUtilisateur } from '../services/projetService';
import { obtenirConfigurationsParProjet } from '../services/configurationTestService';
import {
  lancerExecutionTest,
  obtenirExecutionsParProjet,
  supprimerExecutionTest,
} from '../services/executionTestService';
import './Executions.css';
import { useNavigate } from 'react-router-dom';
import { useAlertDialog } from '../components/AlertDialogContext';

const UTILISATEUR_ID = 1;

function Executions() {
  const navigate = useNavigate();
  const { demanderConfirmation } = useAlertDialog();
  const [projets, setProjets] = useState([]);
  const [projetSelectionneId, setProjetSelectionneId] = useState('');
  const [configurations, setConfigurations] = useState([]);
  const [configurationSelectionneeId, setConfigurationSelectionneeId] = useState('');
  const [executions, setExecutions] = useState([]);

  const [chargementProjets, setChargementProjets] = useState(true);
  const [chargementConfigurations, setChargementConfigurations] = useState(false);
  const [chargementExecutions, setChargementExecutions] = useState(false);
  const [lancementEnCours, setLancementEnCours] = useState(false);
  const [suppressionId, setSuppressionId] = useState(null);

  const [erreur, setErreur] = useState(null);
  const [succes, setSucces] = useState(null);


  useEffect(() => {
    setChargementProjets(true);

    obtenirProjetsUtilisateur(UTILISATEUR_ID)
      .then((res) => setProjets(res.data))
      .catch((err) => {
        console.error(err);
        setErreur('Impossible de charger les projets.');
      })
      .finally(() => setChargementProjets(false));
  }, []);

  const chargerConfigurationsEtExecutions = (projetId) => {
    if (!projetId) {
      setConfigurations([]);
      setExecutions([]);
      setConfigurationSelectionneeId('');
      return;
    }

    setChargementConfigurations(true);
    setChargementExecutions(true);
    setErreur(null);
    setSucces(null);

    obtenirConfigurationsParProjet(projetId)
      .then((res) => {
        setConfigurations(res.data);
        if (res.data.length > 0) {
          setConfigurationSelectionneeId(String(res.data[0].id));
        } else {
          setConfigurationSelectionneeId('');
        }
      })
      .catch((err) => {
        console.error(err);
        setErreur('Impossible de charger les configurations.');
      })
      .finally(() => setChargementConfigurations(false));

    obtenirExecutionsParProjet(projetId)
      .then((res) => {
        const tri = [...res.data].sort(
          (a, b) => new Date(b.dateDebut || 0) - new Date(a.dateDebut || 0)
        );
        setExecutions(tri);
      })
      .catch((err) => {
        console.error(err);
        setErreur('Impossible de charger les exécutions.');
      })
      .finally(() => setChargementExecutions(false));
  };

  useEffect(() => {
    if (projetSelectionneId) {
      chargerConfigurationsEtExecutions(projetSelectionneId);
    } else {
      setConfigurations([]);
      setExecutions([]);
      setConfigurationSelectionneeId('');
    }
  }, [projetSelectionneId]);

  const configurationSelectionnee = useMemo(() => {
    return configurations.find(
      (c) => String(c.id) === String(configurationSelectionneeId)
    );
  }, [configurations, configurationSelectionneeId]);

  const formatDate = (date) => {
    if (!date) return '—';
    return new Date(date).toLocaleString('fr-FR');
  };

  const formatDuree = (dateDebut, dateFin) => {
    if (!dateDebut || !dateFin) return '—';

    const debut = new Date(dateDebut);
    const fin = new Date(dateFin);
    const diffMs = fin - debut;

    if (diffMs < 0) return '—';

    const totalSec = Math.floor(diffMs / 1000);
    const minutes = Math.floor(totalSec / 60);
    const secondes = totalSec % 60;

    if (minutes === 0) return `${secondes}s`;
    return `${minutes} min ${secondes}s`;
  };

  const libelleConfiguration = (config) => {
    if (!config) return '';

    const types = [];
    if (config.testsUnitaires) types.push('Tests unitaires');
    if (config.testsIntegration) types.push('Intégration');
    if (config.testsApi) types.push('API');

    return `#${config.id} — ${types.join(' + ')}`;
  };

  const badgeStatutClasse = (statut) => {
    switch (statut) {
      case 'TERMINEE':
        return 'badgeSucces';
      case 'ECHOUEE':
        return 'badgeEchec';
      case 'EN_COURS':
        return 'badgeWarning';
      case 'EN_ATTENTE':
        return 'badgeNeutral';
      default:
        return 'badgeNeutral';
    }
  };

  const libelleStatut = (statut) => {
    switch (statut) {
      case 'TERMINEE':
        return 'Réussie';
      case 'ECHOUEE':
        return 'Échouée';
      case 'EN_COURS':
        return 'En cours';
      case 'EN_ATTENTE':
        return 'En attente';
      case 'ANNULEE':
        return 'Annulée';
      default:
        return statut || '—';
    }
  };

  const gererLancement = () => {
    if (!projetSelectionneId || !configurationSelectionneeId) {
      setErreur('Choisis un projet et une configuration.');
      return;
    }

    setLancementEnCours(true);
    setErreur(null);
    setSucces(null);

    lancerExecutionTest(projetSelectionneId, configurationSelectionneeId)
      .then(() => {
        setSucces('Exécution lancée avec succès.');
        chargerConfigurationsEtExecutions(projetSelectionneId);
      })
      .catch((err) => {
        console.error(err);
        setErreur("Impossible de lancer l'exécution.");
      })
      .finally(() => setLancementEnCours(false));
  };

  const gererSuppression = async (execution) => {
    const confirmation = await demanderConfirmation({
      titre: `Supprimer l’exécution #${execution.id} ?`,
      message:
        'Les résultats associés à cette exécution seront également supprimés.',
      texteConfirmation: 'Supprimer',
    });

    if (!confirmation) return;

    setSuppressionId(execution.id);
    setErreur(null);
    setSucces(null);

    try {
      await supprimerExecutionTest(execution.id);

      setExecutions((liste) =>
        liste.filter((element) => element.id !== execution.id)
      );
      setSucces(`L'exécution #${execution.id} a été supprimée.`);
    } catch (err) {
      console.error("Erreur lors de la suppression de l'exécution :", err);
      setErreur(
        err.response?.data?.message ||
          "Impossible de supprimer cette exécution."
      );
    } finally {
      setSuppressionId(null);
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h3 className="cardTitre">Lancer une exécution</h3>

        <div className="executionGrid">
          <div>
            <label className="fieldLabel">Choisir un projet</label>
            <select
              className="select"
              value={projetSelectionneId}
              onChange={(e) => setProjetSelectionneId(e.target.value)}
              disabled={chargementProjets}
            >
              <option value="">-- Sélectionne un projet --</option>
              {projets.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nom}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="fieldLabel">Choisir une configuration</label>
            <select
              className="select"
              value={configurationSelectionneeId}
              onChange={(e) => setConfigurationSelectionneeId(e.target.value)}
              disabled={!projetSelectionneId || chargementConfigurations}
            >
              <option value="">-- Sélectionne une configuration --</option>
              {configurations.map((config) => (
                <option key={config.id} value={config.id}>
                  {libelleConfiguration(config)}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div style={{ marginTop: '20px' }}>
          <div className="fieldLabel">Tests inclus dans la configuration</div>

          <div className="badgesRow">
            <span className={`typeBadge ${configurationSelectionnee?.testsUnitaires ? 'typeBadgeActive' : 'typeBadgeInactive'}`}>
              Tests unitaires
            </span>
            <span className={`typeBadge ${configurationSelectionnee?.testsIntegration ? 'typeBadgeActive' : 'typeBadgeInactive'}`}>
              Tests d'intégration
            </span>
            <span className={`typeBadge ${configurationSelectionnee?.testsApi ? 'typeBadgeActive' : 'typeBadgeInactive'}`}>
              Tests API
            </span>
          </div>
        </div>

        <div className="executionFooterActions">
          <button
            className="boutonPrincipal"
            onClick={gererLancement}
            disabled={lancementEnCours || !projetSelectionneId || !configurationSelectionneeId}
          >
            <Play size={16} />
            {lancementEnCours ? 'Lancement...' : "Lancer l'exécution"}
          </button>
        </div>

        {erreur && <div className="messageErreur">{erreur}</div>}
        {succes && <div className="messageSucces">{succes}</div>}
      </div>

      <div className="card">
        <h3 className="cardTitre">Dernières exécutions</h3>

        <div className="tableCard">
          <table className="table">
            <thead>
              <tr className="headerRow">
                <th className="th">Projet</th>
                <th className="th">Configuration</th>
                <th className="th">Statut</th>
                <th className="th">Début</th>
                <th className="th">Durée</th>
                <th className="th">Actions</th>
              </tr>
            </thead>

            <tbody>
              {chargementExecutions && (
                <tr>
                  <td colSpan={6} className="emptyState">
                    Chargement des exécutions...
                  </td>
                </tr>
              )}

              {!chargementExecutions && executions.length === 0 && (
                <tr>
                  <td colSpan={6} className="emptyState">
                    Aucune exécution pour ce projet.
                  </td>
                </tr>
              )}

              {!chargementExecutions &&
                executions.map((execution) => (
                  <tr key={execution.id} className="row">
                    <td className="td">{execution.projet?.nom || '—'}</td>
                    <td className="td">
                      {execution.configurationTest
                        ? libelleConfiguration(execution.configurationTest)
                        : '—'}
                    </td>
                    <td className="td">
                      <span className={`badge ${badgeStatutClasse(execution.statut)}`}>
                        {libelleStatut(execution.statut)}
                      </span>
                    </td>
                    <td className="td">{formatDate(execution.dateDebut)}</td>
                    <td className="td">{formatDuree(execution.dateDebut, execution.dateFin)}</td>
                    <td className="td">
                      <div className="executionActions">
                        <button
                          type="button"
                          className="executionActionButton executionViewButton"
                          title="Voir les résultats"
                          aria-label={`Voir les résultats de l'exécution ${execution.id}`}
                          onClick={() =>
                            navigate(`/resultats?executionId=${execution.id}`)
                          }
                        >
                          <Eye size={18} />
                        </button>

                        <button
                          type="button"
                          className="executionActionButton executionDeleteButton"
                          title={
                            execution.statut === 'EN_COURS'
                              ? 'Une exécution en cours ne peut pas être supprimée'
                              : "Supprimer l'exécution"
                          }
                          aria-label={`Supprimer l'exécution ${execution.id}`}
                          disabled={
                            execution.statut === 'EN_COURS' ||
                            suppressionId === execution.id
                          }
                          onClick={() => gererSuppression(execution)}
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
}

export default Executions;
