import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  BarChart3,
  CheckCircle2,
  Download,
  Eye,
  FileText,
  FlaskConical,
  LoaderCircle,
  RefreshCw,
  Search,
  Trash2,
  TriangleAlert,
  X,
} from 'lucide-react';
import { useAlertDialog } from '../../components/AlertDialogContext';
import { obtenirProjetsAdministration } from '../../services/adminProjetService';
import {
  enregistrerRapportTelecharge,
  obtenirRapportAdministration,
  obtenirRapportsAdministration,
  supprimerRapportAdministration,
  telechargerRapportAdministration,
} from '../../services/adminRapportService';
import { obtenirUtilisateurs } from '../../services/adminUtilisateurService';
import './AdminRapports.css';

const LIBELLES_TYPE = {
  TESTS: 'Tests',
  ANALYSE_QUALITE: 'Analyse qualité',
};

const formaterDate = (date) => {
  if (!date) return '—';

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(date));
};

const formaterTaille = (taille) => {
  if (taille === null || taille === undefined) return '—';
  if (taille < 1024) return `${taille} o`;
  if (taille < 1024 * 1024) return `${(taille / 1024).toFixed(1)} Ko`;
  return `${(taille / (1024 * 1024)).toFixed(1)} Mo`;
};

const extraireMessageErreur = (erreur, messageParDefaut) =>
  erreur.response?.data?.message || messageParDefaut;

function AdminRapports() {
  const { demanderConfirmation } = useAlertDialog();
  const [rapports, setRapports] = useState([]);
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [projets, setProjets] = useState([]);
  const [recherche, setRecherche] = useState('');
  const [utilisateurId, setUtilisateurId] = useState('TOUS');
  const [projetId, setProjetId] = useState('TOUS');
  const [type, setType] = useState('TOUS');
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');
  const [notification, setNotification] = useState(null);
  const [actionEnCours, setActionEnCours] = useState(null);
  const [rapportSelectionne, setRapportSelectionne] = useState(null);
  const [chargementDetail, setChargementDetail] = useState(false);

  const projetsDisponibles = useMemo(() => {
    if (utilisateurId === 'TOUS') return projets;

    return projets.filter((projet) =>
      String(projet.utilisateur?.id) === String(utilisateurId)
    );
  }, [projets, utilisateurId]);

  const chargerRapports = useCallback(async () => {
    setChargement(true);
    setErreur('');

    try {
      const donnees = await obtenirRapportsAdministration({
        recherche: recherche.trim(),
        utilisateurId,
        projetId,
        type,
      });
      setRapports(donnees);
    } catch (erreurRequete) {
      setErreur(extraireMessageErreur(
        erreurRequete,
        'Impossible de charger les rapports.'
      ));
    } finally {
      setChargement(false);
    }
  }, [recherche, utilisateurId, projetId, type]);

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
    const delai = window.setTimeout(chargerRapports, 300);
    return () => window.clearTimeout(delai);
  }, [chargerRapports]);

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

  const consulterRapport = async (rapport) => {
    setRapportSelectionne(rapport);
    setChargementDetail(true);

    try {
      const detail = await obtenirRapportAdministration(rapport.id);
      setRapportSelectionne(detail);
    } catch (erreurRequete) {
      setRapportSelectionne(null);
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de consulter ce rapport.'
        ),
      });
    } finally {
      setChargementDetail(false);
    }
  };

  const telechargerRapport = async (rapport) => {
    setActionEnCours(`download-${rapport.id}`);
    setNotification(null);

    try {
      const fichier = await telechargerRapportAdministration(rapport.id);
      enregistrerRapportTelecharge(fichier, rapport.nom || `rapport-${rapport.id}`);
      setNotification({
        type: 'succes',
        message: 'Rapport téléchargé avec succès.',
      });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de télécharger ce rapport.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  const supprimerRapport = async (rapport) => {
    const confirme = await demanderConfirmation({
      titre: 'Supprimer définitivement ce rapport ?',
      message: `Le rapport « ${rapport.nom || `#${rapport.id}`} » et son fichier PDF seront supprimés. Cette action est irréversible.`,
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    setActionEnCours(`delete-${rapport.id}`);
    setNotification(null);

    try {
      await supprimerRapportAdministration(rapport.id);
      setRapports((anciens) => anciens.filter((element) => element.id !== rapport.id));
      setRapportSelectionne((selection) => selection?.id === rapport.id ? null : selection);
      setNotification({
        type: 'succes',
        message: 'Rapport supprimé avec succès.',
      });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de supprimer ce rapport.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  return (
    <section className="admin-reports-content">
      <div className="admin-reports-toolbar">
        <div className="admin-reports-count">
          <FileText size={19} />
          <strong>{rapports.length}</strong>
          <span>rapport{rapports.length > 1 ? 's' : ''}</span>
        </div>
        <button
          type="button"
          className="admin-reports-refresh"
          onClick={chargerRapports}
          disabled={chargement}
        >
          <RefreshCw size={17} className={chargement ? 'admin-report-spin' : ''} />
          Actualiser
        </button>
      </div>

      {notification && (
        <div className={`admin-reports-notification is-${notification.type}`} role="status">
          {notification.type === 'succes'
            ? <CheckCircle2 size={19} />
            : <TriangleAlert size={19} />}
          <span>{notification.message}</span>
          <button type="button" aria-label="Fermer" onClick={() => setNotification(null)}>
            <X size={16} />
          </button>
        </div>
      )}

      <section className="admin-reports-filters" aria-label="Filtres des rapports">
        <label className="admin-report-search">
          <span>Recherche</span>
          <div>
            <Search size={18} />
            <input
              type="search"
              value={recherche}
              onChange={(evenement) => setRecherche(evenement.target.value)}
              placeholder="Rapport, projet ou propriétaire..."
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
          <span>Type</span>
          <select value={type} onChange={(evenement) => setType(evenement.target.value)}>
            <option value="TOUS">Tous les types</option>
            <option value="TESTS">Tests</option>
            <option value="ANALYSE_QUALITE">Analyse qualité</option>
          </select>
        </label>
      </section>

      {chargement && (
        <div className="admin-reports-feedback">
          <LoaderCircle className="admin-report-spin" size={27} />
          Chargement des rapports...
        </div>
      )}

      {erreur && !chargement && (
        <div className="admin-reports-feedback is-error">
          <TriangleAlert size={25} />
          <strong>Le chargement a échoué</strong>
          <span>{erreur}</span>
          <button type="button" onClick={chargerRapports}>Réessayer</button>
        </div>
      )}

      {!chargement && !erreur && (
        <section className="admin-reports-table-card">
          {rapports.length === 0 ? (
            <div className="admin-reports-empty">
              <FileText size={32} />
              <strong>Aucun rapport trouvé</strong>
              <span>Essayez de modifier les critères de recherche.</span>
            </div>
          ) : (
            <div className="admin-reports-table-scroll">
              <table className="admin-reports-table">
                <thead>
                  <tr>
                    <th>Rapport</th>
                    <th>Projet</th>
                    <th>Propriétaire</th>
                    <th>Type</th>
                    <th>Taille</th>
                    <th>Date</th>
                    <th className="admin-report-actions-heading">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rapports.map((rapport) => {
                    const projet = rapport.execution?.projet;
                    const proprietaire = projet?.utilisateur;
                    const telechargement = actionEnCours === `download-${rapport.id}`;
                    const suppression = actionEnCours === `delete-${rapport.id}`;
                    const IconeType = rapport.type === 'TESTS' ? FlaskConical : BarChart3;

                    return (
                      <tr key={rapport.id}>
                        <td>
                          <div className="admin-report-identity">
                            <span><FileText size={18} /></span>
                            <div>
                              <strong>{rapport.nom || `Rapport #${rapport.id}`}</strong>
                              <small>Exécution #{rapport.execution?.id || '—'}</small>
                            </div>
                          </div>
                        </td>
                        <td><strong className="admin-report-project">{projet?.nom || '—'}</strong></td>
                        <td>
                          <div className="admin-report-owner">
                            <strong>{proprietaire?.nom || 'Utilisateur inconnu'}</strong>
                            <span>{proprietaire?.email || '—'}</span>
                          </div>
                        </td>
                        <td>
                          <span className={`admin-report-type is-${rapport.type?.toLowerCase()}`}>
                            <IconeType size={14} />
                            {LIBELLES_TYPE[rapport.type] || rapport.type}
                          </span>
                        </td>
                        <td>{formaterTaille(rapport.taille)}</td>
                        <td><time>{formaterDate(rapport.dateGeneration)}</time></td>
                        <td>
                          <div className="admin-report-actions">
                            <button
                              type="button"
                              className="is-view"
                              title="Consulter"
                              aria-label={`Consulter ${rapport.nom || `le rapport ${rapport.id}`}`}
                              onClick={() => consulterRapport(rapport)}
                              disabled={telechargement || suppression}
                            >
                              <Eye size={15} />
                            </button>
                            <button
                              type="button"
                              className="is-download"
                              title={rapport.cheminFichier ? 'Télécharger le PDF' : 'Aucun fichier disponible'}
                              aria-label={`Télécharger ${rapport.nom || `le rapport ${rapport.id}`}`}
                              onClick={() => telechargerRapport(rapport)}
                              disabled={!rapport.cheminFichier || telechargement || suppression}
                            >
                              {telechargement
                                ? <LoaderCircle size={15} className="admin-report-spin" />
                                : <Download size={15} />}
                            </button>
                            <button
                              type="button"
                              className="is-delete"
                              title="Supprimer"
                              aria-label={`Supprimer ${rapport.nom || `le rapport ${rapport.id}`}`}
                              onClick={() => supprimerRapport(rapport)}
                              disabled={telechargement || suppression}
                            >
                              {suppression
                                ? <LoaderCircle size={15} className="admin-report-spin" />
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

      {rapportSelectionne && (
        <div
          className="admin-report-modal-overlay"
          role="presentation"
          onMouseDown={(evenement) => {
            if (evenement.target === evenement.currentTarget && !chargementDetail) {
              setRapportSelectionne(null);
            }
          }}
        >
          <section
            className="admin-report-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-report-modal-title"
          >
            <header>
              <div>
                <span>Détails du rapport</span>
                <h2 id="admin-report-modal-title">
                  {rapportSelectionne.nom || `Rapport #${rapportSelectionne.id}`}
                </h2>
              </div>
              <button
                type="button"
                aria-label="Fermer"
                onClick={() => setRapportSelectionne(null)}
                disabled={chargementDetail}
              >
                <X size={19} />
              </button>
            </header>

            {chargementDetail ? (
              <div className="admin-report-modal-loading">
                <LoaderCircle size={26} className="admin-report-spin" />
                Chargement des détails...
              </div>
            ) : (
              <div className="admin-report-details">
                <div>
                  <span>Type</span>
                  <strong>{LIBELLES_TYPE[rapportSelectionne.type] || rapportSelectionne.type}</strong>
                </div>
                <div>
                  <span>Date de génération</span>
                  <strong>{formaterDate(rapportSelectionne.dateGeneration)}</strong>
                </div>
                <div>
                  <span>Projet</span>
                  <strong>{rapportSelectionne.execution?.projet?.nom || '—'}</strong>
                </div>
                <div>
                  <span>Exécution</span>
                  <strong>#{rapportSelectionne.execution?.id || '—'}</strong>
                </div>
                <div>
                  <span>Propriétaire</span>
                  <strong>{rapportSelectionne.execution?.projet?.utilisateur?.nom || 'Utilisateur inconnu'}</strong>
                  <small>{rapportSelectionne.execution?.projet?.utilisateur?.email || '—'}</small>
                </div>
                <div>
                  <span>Taille du fichier</span>
                  <strong>{formaterTaille(rapportSelectionne.taille)}</strong>
                </div>
              </div>
            )}

            {!chargementDetail && (
              <footer>
                <button type="button" className="is-close" onClick={() => setRapportSelectionne(null)}>
                  Fermer
                </button>
                <button
                  type="button"
                  className="is-download"
                  onClick={() => telechargerRapport(rapportSelectionne)}
                  disabled={!rapportSelectionne.cheminFichier || actionEnCours !== null}
                >
                  <Download size={16} /> Télécharger le PDF
                </button>
              </footer>
            )}
          </section>
        </div>
      )}
    </section>
  );
}

export default AdminRapports;
