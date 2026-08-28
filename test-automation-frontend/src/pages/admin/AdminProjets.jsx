import { useCallback, useEffect, useState } from 'react';
import {
  Archive,
  CheckCircle2,
  Eye,
  FolderOpen,
  GitBranch,
  LoaderCircle,
  RefreshCw,
  Search,
  Trash2,
  TriangleAlert,
  X,
} from 'lucide-react';
import { useAlertDialog } from '../../components/AlertDialogContext';
import {
  obtenirProjetAdministration,
  obtenirProjetsAdministration,
  supprimerProjetAdministration,
} from '../../services/admin/adminProjetService';
import { obtenirUtilisateurs } from '../../services/admin/adminUtilisateurService';
import './AdminProjets.css';

const LIBELLES_STATUT = {
  IMPORTE: 'Importé',
  EN_ERREUR: 'En erreur',
  SUPPRIME: 'Supprimé',
};

const LIBELLES_SOURCE = {
  GITHUB: 'GitHub',
  ARCHIVE_ZIP: 'Archive ZIP',
};

const formaterDate = (date) => {
  if (!date) return '—';

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(date));
};

const extraireMessageErreur = (erreur, messageParDefaut) =>
  erreur.response?.data?.message || messageParDefaut;

function AdminProjets() {
  const { demanderConfirmation } = useAlertDialog();
  const [projets, setProjets] = useState([]);
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [recherche, setRecherche] = useState('');
  const [utilisateurId, setUtilisateurId] = useState('TOUS');
  const [statut, setStatut] = useState('TOUS');
  const [typeSource, setTypeSource] = useState('TOUS');
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');
  const [notification, setNotification] = useState(null);
  const [actionEnCours, setActionEnCours] = useState(null);
  const [projetSelectionne, setProjetSelectionne] = useState(null);
  const [chargementDetail, setChargementDetail] = useState(false);

  const chargerProjets = useCallback(async () => {
    setChargement(true);
    setErreur('');

    try {
      const donnees = await obtenirProjetsAdministration({
        recherche: recherche.trim(),
        utilisateurId,
        statut,
        typeSource,
      });
      setProjets(donnees);
    } catch (erreurRequete) {
      setErreur(extraireMessageErreur(
        erreurRequete,
        'Impossible de charger les projets.'
      ));
    } finally {
      setChargement(false);
    }
  }, [recherche, utilisateurId, statut, typeSource]);

  useEffect(() => {
    obtenirUtilisateurs()
      .then((donnees) => setUtilisateurs(donnees))
      .catch(() => setUtilisateurs([]));
  }, []);

  useEffect(() => {
    const delai = window.setTimeout(chargerProjets, 300);
    return () => window.clearTimeout(delai);
  }, [chargerProjets]);

  const consulterProjet = async (projet) => {
    setChargementDetail(true);
    setProjetSelectionne(projet);

    try {
      const detail = await obtenirProjetAdministration(projet.id);
      setProjetSelectionne(detail);
    } catch (erreurRequete) {
      setProjetSelectionne(null);
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de consulter ce projet.'
        ),
      });
    } finally {
      setChargementDetail(false);
    }
  };

  const supprimerProjet = async (projet) => {
    const confirme = await demanderConfirmation({
      titre: 'Supprimer définitivement ce projet ?',
      message: `Le projet « ${projet.nom} », ses configurations, ses exécutions et ses rapports seront supprimés. Cette action est irréversible.`,
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    setActionEnCours(projet.id);
    setNotification(null);

    try {
      await supprimerProjetAdministration(projet.id);
      setProjets((anciens) => anciens.filter((element) => element.id !== projet.id));
      setProjetSelectionne((selection) => selection?.id === projet.id ? null : selection);
      setNotification({
        type: 'succes',
        message: 'Projet supprimé avec succès.',
      });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de supprimer ce projet.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  return (
    <section className="admin-projects-content">
      <div className="admin-projects-toolbar">
        <div className="admin-projects-count">
          <FolderOpen size={19} />
          <strong>{projets.length}</strong>
          <span>projet{projets.length > 1 ? 's' : ''}</span>
        </div>
        <button
          type="button"
          className="admin-projects-refresh"
          onClick={chargerProjets}
          disabled={chargement}
        >
          <RefreshCw size={17} className={chargement ? 'admin-project-spin' : ''} />
          Actualiser
        </button>
      </div>

      {notification && (
        <div className={`admin-projects-notification is-${notification.type}`} role="status">
          {notification.type === 'succes'
            ? <CheckCircle2 size={19} />
            : <TriangleAlert size={19} />}
          <span>{notification.message}</span>
          <button type="button" aria-label="Fermer" onClick={() => setNotification(null)}>
            <X size={16} />
          </button>
        </div>
      )}

      <section className="admin-projects-filters" aria-label="Filtres des projets">
        <label className="admin-project-search">
          <span>Recherche</span>
          <div>
            <Search size={18} />
            <input
              type="search"
              value={recherche}
              onChange={(evenement) => setRecherche(evenement.target.value)}
              placeholder="Nom, description ou propriétaire..."
            />
          </div>
        </label>

        <label>
          <span>Propriétaire</span>
          <select value={utilisateurId} onChange={(evenement) => setUtilisateurId(evenement.target.value)}>
            <option value="TOUS">Tous les propriétaires</option>
            {utilisateurs.map((utilisateur) => (
              <option key={utilisateur.id} value={utilisateur.id}>
                {utilisateur.nom || utilisateur.email}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>Statut</span>
          <select value={statut} onChange={(evenement) => setStatut(evenement.target.value)}>
            <option value="TOUS">Tous les statuts</option>
            <option value="IMPORTE">Importé</option>
            <option value="EN_ERREUR">En erreur</option>
            <option value="SUPPRIME">Supprimé</option>
          </select>
        </label>

        <label>
          <span>Source</span>
          <select value={typeSource} onChange={(evenement) => setTypeSource(evenement.target.value)}>
            <option value="TOUS">Toutes les sources</option>
            <option value="GITHUB">GitHub</option>
            <option value="ARCHIVE_ZIP">Archive ZIP</option>
          </select>
        </label>
      </section>

      {chargement && (
        <div className="admin-projects-feedback">
          <LoaderCircle className="admin-project-spin" size={27} />
          Chargement des projets...
        </div>
      )}

      {erreur && !chargement && (
        <div className="admin-projects-feedback is-error">
          <TriangleAlert size={25} />
          <strong>Le chargement a échoué</strong>
          <span>{erreur}</span>
          <button type="button" onClick={chargerProjets}>Réessayer</button>
        </div>
      )}

      {!chargement && !erreur && (
        <section className="admin-projects-table-card">
          {projets.length === 0 ? (
            <div className="admin-projects-empty">
              <FolderOpen size={32} />
              <strong>Aucun projet trouvé</strong>
              <span>Essayez de modifier les critères de recherche.</span>
            </div>
          ) : (
            <div className="admin-projects-table-scroll">
              <table className="admin-projects-table">
                <thead>
                  <tr>
                    <th>Projet</th>
                    <th>Propriétaire</th>
                    <th>Source</th>
                    <th>Statut</th>
                    <th>Date d’import</th>
                    <th className="admin-project-actions-heading">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {projets.map((projet) => {
                    const traitement = actionEnCours === projet.id;
                    const IconeSource = projet.typeSource === 'GITHUB' ? GitBranch : Archive;

                    return (
                      <tr key={projet.id}>
                        <td>
                          <div className="admin-project-identity">
                            <span><FolderOpen size={18} /></span>
                            <div>
                              <strong>{projet.nom}</strong>
                              <small>{projet.description || 'Aucune description'}</small>
                            </div>
                          </div>
                        </td>
                        <td>
                          <div className="admin-project-owner">
                            <strong>{projet.utilisateur?.nom || 'Utilisateur inconnu'}</strong>
                            <span>{projet.utilisateur?.email || '—'}</span>
                          </div>
                        </td>
                        <td>
                          <span className="admin-project-source">
                            <IconeSource size={14} />
                            {LIBELLES_SOURCE[projet.typeSource] || projet.typeSource}
                          </span>
                        </td>
                        <td>
                          <span className={`admin-project-status is-${projet.statut?.toLowerCase()}`}>
                            {LIBELLES_STATUT[projet.statut] || projet.statut}
                          </span>
                        </td>
                        <td><time>{formaterDate(projet.dateImport)}</time></td>
                        <td>
                          <div className="admin-project-actions">
                            <button
                              type="button"
                              className="is-view"
                              title="Consulter"
                              aria-label={`Consulter ${projet.nom}`}
                              onClick={() => consulterProjet(projet)}
                              disabled={traitement}
                            >
                              <Eye size={15} />
                            </button>
                            <button
                              type="button"
                              className="is-delete"
                              title="Supprimer"
                              aria-label={`Supprimer ${projet.nom}`}
                              onClick={() => supprimerProjet(projet)}
                              disabled={traitement}
                            >
                              {traitement
                                ? <LoaderCircle size={15} className="admin-project-spin" />
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

      {projetSelectionne && (
        <div
          className="admin-project-modal-overlay"
          role="presentation"
          onMouseDown={(evenement) => {
            if (evenement.target === evenement.currentTarget && !chargementDetail) {
              setProjetSelectionne(null);
            }
          }}
        >
          <section
            className="admin-project-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-project-modal-title"
          >
            <header>
              <div>
                <span>Détails du projet</span>
                <h2 id="admin-project-modal-title">{projetSelectionne.nom}</h2>
              </div>
              <button
                type="button"
                aria-label="Fermer"
                onClick={() => setProjetSelectionne(null)}
                disabled={chargementDetail}
              >
                <X size={19} />
              </button>
            </header>

            {chargementDetail ? (
              <div className="admin-project-modal-loading">
                <LoaderCircle size={26} className="admin-project-spin" />
                Chargement des détails...
              </div>
            ) : (
              <div className="admin-project-details">
                <div className="is-wide">
                  <span>Description</span>
                  <p>{projetSelectionne.description || 'Aucune description renseignée.'}</p>
                </div>
                <div>
                  <span>Propriétaire</span>
                  <strong>{projetSelectionne.utilisateur?.nom || 'Utilisateur inconnu'}</strong>
                  <small>{projetSelectionne.utilisateur?.email || '—'}</small>
                </div>
                <div>
                  <span>Date d’import</span>
                  <strong>{formaterDate(projetSelectionne.dateImport)}</strong>
                </div>
                <div>
                  <span>Source</span>
                  <strong>{LIBELLES_SOURCE[projetSelectionne.typeSource] || projetSelectionne.typeSource}</strong>
                </div>
                <div>
                  <span>Statut</span>
                  <strong>{LIBELLES_STATUT[projetSelectionne.statut] || projetSelectionne.statut}</strong>
                </div>
                <div className="is-wide">
                  <span>Clé du projet</span>
                  <code>{projetSelectionne.projectKey || '—'}</code>
                </div>
              </div>
            )}

            {!chargementDetail && (
              <footer>
                <button type="button" onClick={() => setProjetSelectionne(null)}>Fermer</button>
              </footer>
            )}
          </section>
        </div>
      )}
    </section>
  );
}

export default AdminProjets;
