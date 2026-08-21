import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Search,
  CheckCircle2,
  XCircle,
  MinusCircle,
  Clock,
  Download,
} from 'lucide-react';

import { obtenirProjetsUtilisateur } from '../services/projetService';
import { obtenirExecutionsParProjet } from '../services/executionTestService';
import { obtenirResultatsParExecution } from '../services/resultatTestService';
import {
  genererRapportTests,
  telechargerRapport,
  enregistrerFichierRapport,
} from '../services/rapportService';

import './ResultatsTests.css';

const UTILISATEUR_ID = 1;
const RESULTATS_PAR_PAGE = 10;

function ResultatsTests() {
  const [searchParams] = useSearchParams();

  const [executions, setExecutions] = useState([]);
  const [executionId, setExecutionId] = useState('');
  const [resultats, setResultats] = useState([]);

  const [chargementExecutions, setChargementExecutions] = useState(true);
  const [chargementResultats, setChargementResultats] = useState(false);
  const [generationRapport, setGenerationRapport] = useState(false);

  const [erreur, setErreur] = useState(null);
  const [succesRapport, setSuccesRapport] = useState(null);

  const [recherche, setRecherche] = useState('');
  const [typeFiltre, setTypeFiltre] = useState('TOUS');
  const [statutFiltre, setStatutFiltre] = useState('TOUS');
  const [page, setPage] = useState(1);

  /*
   * Chargement de toutes les exécutions
   * de tous les projets de l'utilisateur.
   */
  useEffect(() => {
    setChargementExecutions(true);
    setErreur(null);

    obtenirProjetsUtilisateur(UTILISATEUR_ID)
      .then(async (res) => {
        const projets = res.data;

        const appels = projets.map(async (projet) => {
          try {
            const response = await obtenirExecutionsParProjet(projet.id);

            return response.data.map((execution) => ({
              ...execution,

              // Permet d'afficher le projet même si
              // execution.projet n'est pas renvoyé.
              projetAffiche: execution.projet || projet,
            }));
          } catch (error) {
            console.error(
              `Erreur chargement exécutions projet ${projet.id}`,
              error
            );

            return [];
          }
        });

        const groupes = await Promise.all(appels);

        const toutesLesExecutions = groupes
          .flat()
          .sort(
            (a, b) =>
              new Date(b.dateDebut || 0) -
              new Date(a.dateDebut || 0)
          );

        setExecutions(toutesLesExecutions);

        /*
         * Si on vient de la page Exécutions :
         * /resultats?executionId=17
         */
        const idUrl = searchParams.get('executionId');

        if (
          idUrl &&
          toutesLesExecutions.some(
            (e) => String(e.id) === String(idUrl)
          )
        ) {
          setExecutionId(String(idUrl));
        } else if (toutesLesExecutions.length > 0) {
          setExecutionId(String(toutesLesExecutions[0].id));
        }
      })
      .catch((err) => {
        console.error(err);
        setErreur('Impossible de charger les exécutions.');
      })
      .finally(() => {
        setChargementExecutions(false);
      });
  }, [searchParams]);

  /*
   * Chargement des résultats
   * de l'exécution sélectionnée.
   */
  useEffect(() => {
    if (!executionId) {
      setResultats([]);
      return;
    }

    setChargementResultats(true);
    setErreur(null);
    setPage(1);

    obtenirResultatsParExecution(executionId)
      .then((res) => {
        setResultats(res.data);
      })
      .catch((err) => {
        console.error(err);
        setErreur(
          'Impossible de charger les résultats des tests.'
        );
      })
      .finally(() => {
        setChargementResultats(false);
      });
  }, [executionId]);

  /*
   * Exécution actuellement sélectionnée.
   */
  const executionSelectionnee = useMemo(
    () =>
      executions.find(
        (e) => String(e.id) === String(executionId)
      ),
    [executions, executionId]
  );

  /*
   * Format de date.
   */
  const formaterDate = (date) => {
    if (!date) return '—';

    return new Date(date).toLocaleString('fr-FR');
  };

  /*
   * Durée totale de l'exécution.
   */
  const formaterDureeExecution = (dateDebut, dateFin) => {
    if (!dateDebut || !dateFin) return '—';

    const diff = new Date(dateFin) - new Date(dateDebut);

    if (diff < 0) return '—';

    const secondes = Math.floor(diff / 1000);
    const minutes = Math.floor(secondes / 60);
    const reste = secondes % 60;

    if (minutes === 0) {
      return `${reste} s`;
    }

    return `${minutes} min ${reste} s`;
  };

  /*
   * Affichage de la configuration.
   */
  const configurationLabel = (config) => {
    if (!config) return '—';

    const types = [];

    if (config.testsUnitaires) {
      types.push('Tests unitaires');
    }

    if (config.testsIntegration) {
      types.push('Intégration');
    }

    if (config.testsApi) {
      types.push('API');
    }

    return `#${config.id} — ${types.join(' + ')}`;
  };

  /*
   * Libellé du type de test.
   */
  const typeLabel = (type) => {
    switch (type) {
      case 'UNITAIRE':
        return 'Unitaire';

      case 'INTEGRATION':
        return 'Intégration';

      case 'API':
        return 'API';

      default:
        return type || '—';
    }
  };

  /*
   * Libellé du statut.
   */
  const statutLabel = (statut) => {
    switch (statut) {
      case 'REUSSI':
        return 'Réussi';

      case 'ECHOUE':
        return 'Échoué';

      case 'IGNORED':
        return 'Ignoré';

      default:
        return statut || '—';
    }
  };

  /*
   * Statistiques.
   */
  const totalTests = resultats.length;

  const testsReussis = resultats.filter(
    (r) => r.statut === 'REUSSI'
  ).length;

  const testsEchoues = resultats.filter(
    (r) => r.statut === 'ECHOUE'
  ).length;

  const testsIgnores = resultats.filter(
    (r) => r.statut === 'IGNORED'
  ).length;

  const dureeTotale = resultats.reduce(
    (somme, resultat) => somme + (resultat.duree || 0),
    0
  );

  const pourcentage = (nombre) => {
    if (totalTests === 0) return 0;

    return Math.round((nombre / totalTests) * 1000) / 10;
  };

  /*
   * Recherche + filtres.
   */
  const resultatsFiltres = useMemo(() => {
    return resultats.filter((resultat) => {
      const texte =
        `${resultat.nomTest || ''} ${resultat.message || ''}`.toLowerCase();

      const rechercheOk =
        texte.includes(recherche.toLowerCase());

      const typeOk =
        typeFiltre === 'TOUS' ||
        resultat.type === typeFiltre;

      const statutOk =
        statutFiltre === 'TOUS' ||
        resultat.statut === statutFiltre;

      return rechercheOk && typeOk && statutOk;
    });
  }, [
    resultats,
    recherche,
    typeFiltre,
    statutFiltre,
  ]);

  /*
   * Pagination.
   */
  const totalPages =
    Math.ceil(
      resultatsFiltres.length / RESULTATS_PAR_PAGE
    ) || 1;

  const debut = (page - 1) * RESULTATS_PAR_PAGE;

  const resultatsPage = resultatsFiltres.slice(
    debut,
    debut + RESULTATS_PAR_PAGE
  );

  /*
   * Génération du rapport.
   */
  const gererGenerationRapport = async () => {
    if (!executionId) return;

    setGenerationRapport(true);
    setErreur(null);
    setSuccesRapport(null);

    try {
      const generation = await genererRapportTests(executionId);
      const rapport = generation.data;
      const telechargement = await telechargerRapport(rapport.id);

      enregistrerFichierRapport(
        telechargement.data,
        rapport.nom || `rapport_tests_execution_${executionId}`
      );

      setSuccesRapport(
        'Le rapport PDF a été généré et téléchargé.'
      );
    } catch (err) {
      console.error(
        'Erreur lors de la génération du rapport :',
        err
      );

      setErreur(
        err.response?.data?.message ||
          "Impossible de générer le rapport de cette exécution."
      );
    } finally {
      setGenerationRapport(false);
    }
  };

  return (
    <div className="rt-page">

      {/* ====================================
          Informations sur l'exécution
      ==================================== */}

      <div className="rt-card rt-execution-card">

        <div className="rt-execution-item rt-execution-select">
          <label className="rt-small-label">
            Exécution
          </label>

          <select
            className="rt-select"
            value={executionId}
            disabled={chargementExecutions}
            onChange={(e) => {
              setExecutionId(e.target.value);
              setSuccesRapport(null);
            }}
          >
            {executions.length === 0 && (
              <option value="">
                Aucune exécution
              </option>
            )}

            {executions.map((execution) => (
              <option
                key={execution.id}
                value={execution.id}
              >
                #{execution.id} - {formaterDate(execution.dateDebut)}
              </option>
            ))}
          </select>

          {executionSelectionnee && (
            <span
              className={`rt-execution-status rt-execution-${executionSelectionnee.statut?.toLowerCase()}`}
            >
              {executionSelectionnee.statut}
            </span>
          )}
        </div>

        {/* Projet */}
        <div className="rt-execution-item">
          <span className="rt-small-label">
            Projet
          </span>

          <strong>
            {executionSelectionnee?.projetAffiche?.nom || '—'}
          </strong>
        </div>

        {/* Configuration */}
        <div className="rt-execution-item rt-config-column">
          <span className="rt-small-label">
            Configuration
          </span>

          <strong>
            {configurationLabel(
              executionSelectionnee?.configurationTest
            )}
          </strong>

          <div className="rt-config-types">

            {executionSelectionnee?.configurationTest?.testsUnitaires && (
              <span className="rt-config-badge">
                ✓ Tests unitaires
              </span>
            )}

            {executionSelectionnee?.configurationTest?.testsIntegration && (
              <span className="rt-config-badge">
                ✓ Intégration
              </span>
            )}

            {executionSelectionnee?.configurationTest?.testsApi && (
              <span className="rt-config-badge">
                ✓ API
              </span>
            )}

          </div>
        </div>

        {/* Début */}
        <div className="rt-execution-item">
          <span className="rt-small-label">
            Début
          </span>

          <strong>
            {formaterDate(
              executionSelectionnee?.dateDebut
            )}
          </strong>
        </div>

        {/* Fin */}
        <div className="rt-execution-item">
          <span className="rt-small-label">
            Fin
          </span>

          <strong>
            {formaterDate(
              executionSelectionnee?.dateFin
            )}
          </strong>
        </div>

        {/* Durée */}
        <div className="rt-execution-item">
          <span className="rt-small-label">
            Durée
          </span>

          <strong>
            {formaterDureeExecution(
              executionSelectionnee?.dateDebut,
              executionSelectionnee?.dateFin
            )}
          </strong>
        </div>

      </div>

      {/* ====================================
          Cartes statistiques
      ==================================== */}

      <div className="rt-card">
        <div className="rt-stats">

          {/* Total */}
          <div className="rt-stat rt-stat-total">
            <div>
              <span>Total des tests</span>
              <strong>{totalTests}</strong>
            </div>
          </div>

          {/* Réussis */}
          <div className="rt-stat rt-stat-success">
            <div>
              <span>Réussis</span>
              <strong>{testsReussis}</strong>
              <small>{pourcentage(testsReussis)}%</small>
            </div>

            <CheckCircle2 size={30} />
          </div>

          {/* Échoués */}
          <div className="rt-stat rt-stat-error">
            <div>
              <span>Échoués</span>
              <strong>{testsEchoues}</strong>
              <small>{pourcentage(testsEchoues)}%</small>
            </div>

            <XCircle size={30} />
          </div>

          {/* Ignorés */}
          <div className="rt-stat rt-stat-ignored">
            <div>
              <span>Ignorés</span>
              <strong>{testsIgnores}</strong>
              <small>{pourcentage(testsIgnores)}%</small>
            </div>

            <MinusCircle size={30} />
          </div>

          {/* Durée */}
          <div className="rt-stat rt-stat-duration">
            <div>
              <span>Durée totale</span>
              <strong>{dureeTotale} ms</strong>
            </div>

            <Clock size={30} />
          </div>

        </div>
      </div>

      {/* ====================================
          Recherche + filtres + tableau
      ==================================== */}

      <div className="rt-card">

        <div className="rt-toolbar">

          {/* Recherche */}
          <div className="rt-search">
            <Search size={18} />

            <input
              type="text"
              placeholder="Rechercher un test..."
              value={recherche}
              onChange={(e) => {
                setRecherche(e.target.value);
                setPage(1);
              }}
            />
          </div>

          {/* Filtre type */}
          <select
            className="rt-filter"
            value={typeFiltre}
            onChange={(e) => {
              setTypeFiltre(e.target.value);
              setPage(1);
            }}
          >
            <option value="TOUS">Type: Tous</option>
            <option value="UNITAIRE">Unitaire</option>
            <option value="INTEGRATION">Intégration</option>
            <option value="API">API</option>
          </select>

          {/* Filtre statut */}
          <select
            className="rt-filter"
            value={statutFiltre}
            onChange={(e) => {
              setStatutFiltre(e.target.value);
              setPage(1);
            }}
          >
            <option value="TOUS">Statut: Tous</option>
            <option value="REUSSI">Réussi</option>
            <option value="ECHOUE">Échoué</option>
            <option value="IGNORED">Ignoré</option>
          </select>

          <button
            type="button"
            className="rt-report-button"
            aria-label="Générer et télécharger le rapport PDF des tests"
            onClick={gererGenerationRapport}
            disabled={
              generationRapport ||
              chargementResultats ||
              !executionId ||
              resultats.length === 0
            }
          >
            <Download size={17} />

            {generationRapport
              ? 'Génération...'
              : 'Générer le rapport'}
          </button>

        </div>

        {/* Erreur */}
        {erreur && (
          <div className="rt-error">
            {erreur}
          </div>
        )}

        {succesRapport && (
          <div className="rt-success">
            {succesRapport}
          </div>
        )}

        {/* Chargement */}
        {chargementResultats ? (
          <div className="rt-empty">
            Chargement des résultats...
          </div>
        ) : (
          <div className="rt-table-wrapper">

            <table className="rt-table">

              <thead>
                <tr>
                  <th>Nom du test</th>
                  <th>Type</th>
                  <th>Statut</th>
                  <th>Durée</th>
                  <th>Message</th>
                </tr>
              </thead>

              <tbody>

                {resultatsPage.length === 0 && (
                  <tr>
                    <td
                      colSpan={5}
                      className="rt-empty"
                    >
                      Aucun résultat trouvé.
                    </td>
                  </tr>
                )}

                {resultatsPage.map((resultat) => (
                  <tr key={resultat.id}>

                    {/* Nom */}
                    <td className="rt-test-name">
                      {resultat.nomTest}
                    </td>

                    {/* Type */}
                    <td>
                      <span
                        className={`rt-type rt-type-${resultat.type?.toLowerCase()}`}
                      >
                        {typeLabel(resultat.type)}
                      </span>
                    </td>

                    {/* Statut */}
                    <td>
                      <span
                        className={`rt-status rt-status-${resultat.statut?.toLowerCase()}`}
                      >
                        {statutLabel(resultat.statut)}
                      </span>
                    </td>

                    {/* Durée */}
                    <td>
                      {resultat.duree != null
                        ? `${resultat.duree} ms`
                        : '—'}
                    </td>

                    {/* Message */}
                    <td
                      className={
                        resultat.statut === 'ECHOUE'
                          ? 'rt-message-error'
                          : 'rt-message'
                      }
                    >
                      {resultat.message || '—'}
                    </td>

                  </tr>
                ))}

              </tbody>

            </table>

          </div>
        )}

        {/* ====================================
            Pagination
        ==================================== */}

        <div className="rt-footer">

          <span>
            Affichage de{' '}
            {resultatsFiltres.length === 0
              ? 0
              : debut + 1}{' '}
            à{' '}
            {Math.min(
              debut + RESULTATS_PAR_PAGE,
              resultatsFiltres.length
            )}{' '}
            sur {resultatsFiltres.length} résultats
          </span>

          <div className="rt-pagination">

            {/* Précédent */}
            <button
              disabled={page === 1}
              onClick={() =>
                setPage((p) => Math.max(1, p - 1))
              }
            >
              ‹
            </button>

            {/* Pages */}
            {Array.from(
              { length: totalPages },
              (_, i) => i + 1
            ).map((numero) => (
              <button
                key={numero}
                className={
                  page === numero
                    ? 'rt-page-active'
                    : ''
                }
                onClick={() => setPage(numero)}
              >
                {numero}
              </button>
            ))}

            {/* Suivant */}
            <button
              disabled={page === totalPages}
              onClick={() =>
                setPage((p) =>
                  Math.min(totalPages, p + 1)
                )
              }
            >
              ›
            </button>

          </div>
        </div>

      </div>

    </div>
  );
}

export default ResultatsTests;