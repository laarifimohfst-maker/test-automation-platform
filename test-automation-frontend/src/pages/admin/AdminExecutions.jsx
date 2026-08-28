import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  BarChart3,
  CheckCircle2,
  Clock3,
  Eye,
  FlaskConical,
  LoaderCircle,
  Play,
  RefreshCw,
  Search,
  Trash2,
  TriangleAlert,
  X,
} from 'lucide-react';
import { useAlertDialog } from '../../components/AlertDialogContext';
import {
  obtenirExecutionAdministration,
  obtenirExecutionsAdministration,
  supprimerExecutionAdministration,
} from '../../services/admin/adminExecutionService';
import { obtenirProjetsAdministration } from '../../services/admin/adminProjetService';
import { obtenirUtilisateurs } from '../../services/admin/adminUtilisateurService';
import './AdminExecutions.css';

const LIBELLES_STATUT = {
  EN_ATTENTE: 'En attente',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ECHOUEE: 'Échouée',
  ANNULEE: 'Annulée',
};

const formaterDate = (date) => {
  if (!date) return '—';

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(date));
};

const formaterDuree = (dateDebut, dateFin) => {
  if (!dateDebut || !dateFin) return '—';

  const millisecondes = new Date(dateFin) - new Date(dateDebut);
  if (millisecondes < 0) return '—';

  const secondesTotales = Math.floor(millisecondes / 1000);
  const minutes = Math.floor(secondesTotales / 60);
  const secondes = secondesTotales % 60;
  return minutes > 0 ? `${minutes} min ${secondes} s` : `${secondes} s`;
};

const obtenirType = (execution) =>
  execution.configurationTest ? 'TESTS' : 'ANALYSE_QUALITE';

const libelleConfiguration = (configuration) => {
  if (!configuration) return '—';

  const types = [];
  if (configuration.testsUnitaires) types.push('Unitaires');
  if (configuration.testsIntegration) types.push('Intégration');
  if (configuration.testsApi) types.push('API');
  return types.length > 0 ? types.join(' + ') : `Configuration #${configuration.id}`;
};

const extraireMessageErreur = (erreur, messageParDefaut) =>
  erreur.response?.data?.message || messageParDefaut;

function AdminExecutions() {
  const { demanderConfirmation } = useAlertDialog();
  const [executions, setExecutions] = useState([]);
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [projets, setProjets] = useState([]);
  const [recherche, setRecherche] = useState('');
  const [utilisateurId, setUtilisateurId] = useState('TOUS');
  const [projetId, setProjetId] = useState('TOUS');
  const [statut, setStatut] = useState('TOUS');
  const [type, setType] = useState('TOUS');
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');
  const [notification, setNotification] = useState(null);
  const [actionEnCours, setActionEnCours] = useState(null);
  const [executionSelectionnee, setExecutionSelectionnee] = useState(null);
  const [chargementDetail, setChargementDetail] = useState(false);

  const projetsDisponibles = useMemo(() => {
    if (utilisateurId === 'TOUS') return projets;

    return projets.filter((projet) =>
      String(projet.utilisateur?.id) === String(utilisateurId)
    );
  }, [projets, utilisateurId]);

  const chargerExecutions = useCallback(async () => {
    setChargement(true);
    setErreur('');

    try {
      const donnees = await obtenirExecutionsAdministration({
        recherche: recherche.trim(),
        utilisateurId,
        projetId,
        statut,
        type,
      });
      setExecutions(donnees);
    } catch (erreurRequete) {
      setErreur(extraireMessageErreur(
        erreurRequete,
        'Impossible de charger les exécutions.'
      ));
    } finally {
      setChargement(false);
    }
  }, [recherche, utilisateurId, projetId, statut, type]);

  useEffect(() => {
    Promise.all([
      obtenirUtilisateurs(),
      obtenirProjetsAdministration(),
    ])
      .then(([donneesUtilisateurs, donneesProjets]) => {
        setUtilisateurs(donneesUtilisateurs);
        setProjets(donneesProjets);
      })
      .catch(() => {
        setUtilisateurs([]);
        setProjets([]);
      });
  }, []);

  useEffect(() => {
    const delai = window.setTimeout(chargerExecutions, 300);
    return () => window.clearTimeout(delai);
  }, [chargerExecutions]);

  const changerProprietaire = (evenement) => {
    const nouvelUtilisateurId = evenement.target.value;
    setUtilisateurId(nouvelUtilisateurId);

    if (
      projetId !== 'TOUS'
      && nouvelUtilisateurId !== 'TOUS'
      && !projets.some((projet) =>
        String(projet.id) === String(projetId)
        && String(projet.utilisateur?.id) === String(nouvelUtilisateurId)
      )
    ) {
      setProjetId('TOUS');
    }
  };

  const consulterExecution = async (execution) => {
    setExecutionSelectionnee(execution);
    setChargementDetail(true);

    try {
      const detail = await obtenirExecutionAdministration(execution.id);
      setExecutionSelectionnee(detail);
    } catch (erreurRequete) {
      setExecutionSelectionnee(null);
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de consulter cette exécution.'
        ),
      });
    } finally {
      setChargementDetail(false);
    }
  };

  const supprimerExecution = async (execution) => {
    const confirme = await demanderConfirmation({
      titre: `Supprimer l’exécution #${execution.id} ?`,
      message: 'Les résultats, l’analyse, le rapport et les notifications associés seront également supprimés. Cette action est irréversible.',
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    setActionEnCours(execution.id);
    setNotification(null);

    try {
      await supprimerExecutionAdministration(execution.id);
      setExecutions((anciens) => anciens.filter((element) => element.id !== execution.id));
      setExecutionSelectionnee((selection) => selection?.id === execution.id ? null : selection);
      setNotification({
        type: 'succes',
        message: `L’exécution #${execution.id} a été supprimée.`,
      });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de supprimer cette exécution.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  return (
    <section className="admin-executions-content">
      <div className="admin-executions-toolbar">
        <div className="admin-executions-count">
          <Play size={19} />
          <strong>{executions.length}</strong>
          <span>exécution{executions.length > 1 ? 's' : ''}</span>
        </div>
        <button
          type="button"
          className="admin-executions-refresh"
          onClick={chargerExecutions}
          disabled={chargement}
        >
          <RefreshCw size={17} className={chargement ? 'admin-execution-spin' : ''} />
          Actualiser
        </button>
      </div>

      {notification && (
        <div className={`admin-executions-notification is-${notification.type}`} role="status">
          {notification.type === 'succes'
            ? <CheckCircle2 size={19} />
            : <TriangleAlert size={19} />}
          <span>{notification.message}</span>
          <button type="button" aria-label="Fermer" onClick={() => setNotification(null)}>
            <X size={16} />
          </button>
        </div>
      )}

      <section className="admin-executions-filters" aria-label="Filtres des exécutions">
        <label className="admin-execution-search">
          <span>Recherche</span>
          <div>
            <Search size={18} />
            <input
              type="search"
              value={recherche}
              onChange={(evenement) => setRecherche(evenement.target.value)}
              placeholder="Numéro, projet, propriétaire ou message..."
            />
          </div>
        </label>

        <label>
          <span>Propriétaire</span>
          <select value={utilisateurId} onChange={changerProprietaire}>
            <option value="TOUS">Tous les propriétaires</option>
            {utilisateurs.map((utilisateur) => (
              <option key={utilisateur.id} value={utilisateur.id}>
                {utilisateur.nom || utilisateur.email}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>Projet</span>
          <select value={projetId} onChange={(evenement) => setProjetId(evenement.target.value)}>
            <option value="TOUS">Tous les projets</option>
            {projetsDisponibles.map((projet) => (
              <option key={projet.id} value={projet.id}>{projet.nom}</option>
            ))}
          </select>
        </label>

        <label>
          <span>Statut</span>
          <select value={statut} onChange={(evenement) => setStatut(evenement.target.value)}>
            <option value="TOUS">Tous les statuts</option>
            <option value="EN_ATTENTE">En attente</option>
            <option value="EN_COURS">En cours</option>
            <option value="TERMINEE">Terminée</option>
            <option value="ECHOUEE">Échouée</option>
            <option value="ANNULEE">Annulée</option>
          </select>
        </label>

        <label>
          <span>Type</span>
          <select value={type} onChange={(evenement) => setType(evenement.target.value)}>
            <option value="TOUS">Tous les types</option>
            <option value="TESTS">Tests</option>
            <option value="ANALYSE_QUALITE">Analyse qualité</option>
          </select>
        </label>
      </section>

      {chargement && (
        <div className="admin-executions-feedback">
          <LoaderCircle className="admin-execution-spin" size={27} />
          Chargement des exécutions...
        </div>
      )}

      {erreur && !chargement && (
        <div className="admin-executions-feedback is-error">
          <TriangleAlert size={25} />
          <strong>Le chargement a échoué</strong>
          <span>{erreur}</span>
          <button type="button" onClick={chargerExecutions}>Réessayer</button>
        </div>
      )}

      {!chargement && !erreur && (
        <section className="admin-executions-table-card">
          {executions.length === 0 ? (
            <div className="admin-executions-empty">
              <Clock3 size={32} />
              <strong>Aucune exécution trouvée</strong>
              <span>Essayez de modifier les critères de recherche.</span>
            </div>
          ) : (
            <div className="admin-executions-table-scroll">
              <table className="admin-executions-table">
                <thead>
                  <tr>
                    <th>Exécution</th>
                    <th>Projet</th>
                    <th>Propriétaire</th>
                    <th>Statut</th>
                    <th>Début</th>
                    <th>Durée</th>
                    <th className="admin-execution-actions-heading">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {executions.map((execution) => {
                    const typeExecution = obtenirType(execution);
                    const estTests = typeExecution === 'TESTS';
                    const IconeType = estTests ? FlaskConical : BarChart3;
                    const suppression = actionEnCours === execution.id;

                    return (
                      <tr key={execution.id}>
                        <td>
                          <div className="admin-execution-identity">
                            <span><IconeType size={18} /></span>
                            <div>
                              <strong>Exécution #{execution.id}</strong>
                              <small>{estTests ? 'Tests automatisés' : 'Analyse qualité'}</small>
                            </div>
                          </div>
                        </td>
                        <td><strong className="admin-execution-project">{execution.projet?.nom || '—'}</strong></td>
                        <td>
                          <div className="admin-execution-owner">
                            <strong>{execution.projet?.utilisateur?.nom || 'Utilisateur inconnu'}</strong>
                            <span>{execution.projet?.utilisateur?.email || '—'}</span>
                          </div>
                        </td>
                        <td>
                          <span className={`admin-execution-status is-${execution.statut?.toLowerCase()}`}>
                            {LIBELLES_STATUT[execution.statut] || execution.statut}
                          </span>
                        </td>
                        <td><time>{formaterDate(execution.dateDebut)}</time></td>
                        <td>{formaterDuree(execution.dateDebut, execution.dateFin)}</td>
                        <td>
                          <div className="admin-execution-actions">
                            <button
                              type="button"
                              className="is-view"
                              title="Consulter"
                              aria-label={`Consulter l’exécution ${execution.id}`}
                              onClick={() => consulterExecution(execution)}
                              disabled={suppression}
                            >
                              <Eye size={15} />
                            </button>
                            <button
                              type="button"
                              className="is-delete"
                              title={execution.statut === 'EN_COURS'
                                ? 'Une exécution en cours ne peut pas être supprimée'
                                : 'Supprimer'}
                              aria-label={`Supprimer l’exécution ${execution.id}`}
                              onClick={() => supprimerExecution(execution)}
                              disabled={suppression || execution.statut === 'EN_COURS'}
                            >
                              {suppression
                                ? <LoaderCircle size={15} className="admin-execution-spin" />
                                : <Trash2 size={15} />}
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
        </section>
      )}

      {executionSelectionnee && (
        <div
          className="admin-execution-modal-overlay"
          role="presentation"
          onMouseDown={(evenement) => {
            if (evenement.target === evenement.currentTarget && !chargementDetail) {
              setExecutionSelectionnee(null);
            }
          }}
        >
          <section
            className="admin-execution-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-execution-modal-title"
          >
            <header>
              <div>
                <span>Détails de l’exécution</span>
                <h2 id="admin-execution-modal-title">Exécution #{executionSelectionnee.id}</h2>
              </div>
              <button
                type="button"
                aria-label="Fermer"
                onClick={() => setExecutionSelectionnee(null)}
                disabled={chargementDetail}
              >
                <X size={19} />
              </button>
            </header>

            {chargementDetail ? (
              <div className="admin-execution-modal-loading">
                <LoaderCircle size={26} className="admin-execution-spin" />
                Chargement des détails...
              </div>
            ) : (
              <div className="admin-execution-details">
                <div>
                  <span>Type</span>
                  <strong>{obtenirType(executionSelectionnee) === 'TESTS'
                    ? 'Tests automatisés'
                    : 'Analyse qualité'}</strong>
                </div>
                <div>
                  <span>Statut</span>
                  <strong>{LIBELLES_STATUT[executionSelectionnee.statut] || executionSelectionnee.statut}</strong>
                </div>
                <div>
                  <span>Projet</span>
                  <strong>{executionSelectionnee.projet?.nom || '—'}</strong>
                </div>
                <div>
                  <span>Propriétaire</span>
                  <strong>{executionSelectionnee.projet?.utilisateur?.nom || 'Utilisateur inconnu'}</strong>
                  <small>{executionSelectionnee.projet?.utilisateur?.email || '—'}</small>
                </div>
                <div>
                  <span>Date de début</span>
                  <strong>{formaterDate(executionSelectionnee.dateDebut)}</strong>
                </div>
                <div>
                  <span>Date de fin</span>
                  <strong>{formaterDate(executionSelectionnee.dateFin)}</strong>
                </div>
                <div>
                  <span>Durée</span>
                  <strong>{formaterDuree(executionSelectionnee.dateDebut, executionSelectionnee.dateFin)}</strong>
                </div>
                {executionSelectionnee.configurationTest && (
                  <div>
                    <span>Configuration des tests</span>
                    <strong>{libelleConfiguration(executionSelectionnee.configurationTest)}</strong>
                  </div>
                )}
                <div className="is-wide">
                  <span>Message</span>
                  <p>{executionSelectionnee.message || 'Aucun message renseigné.'}</p>
                </div>
              </div>
            )}

            {!chargementDetail && (
              <footer>
                <button type="button" onClick={() => setExecutionSelectionnee(null)}>Fermer</button>
              </footer>
            )}
          </section>
        </div>
      )}
    </section>
  );
}

export default AdminExecutions;
