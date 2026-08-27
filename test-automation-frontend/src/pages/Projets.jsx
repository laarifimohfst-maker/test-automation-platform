import { useState, useEffect } from 'react';
import { Search, Trash2, Pencil, X } from 'lucide-react';
import {
  obtenirProjetsUtilisateur,
  supprimerProjet,
  modifierProjet,
} from '../services/projetService';
import { useAlertDialog } from '../components/AlertDialogContext';
import './Projets.css';
import { obtenirUtilisateurId } from '../services/authStorage';

const PROJETS_PAR_PAGE = 5;

// Palette de couleurs piochée selon l'id du projet, pour l'avatar coloré
const PALETTE = ['#e0f2fe', '#dcfce7', '#fef3c7', '#dbeafe', '#fee2e2'];

function Projets() {
  const { demanderConfirmation, afficherAlerte } = useAlertDialog();

  const [projets, setProjets] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  const [recherche, setRecherche] = useState('');
  const [filtreStatut, setFiltreStatut] = useState('Tous');
  const [tri, setTri] = useState('recent');
  const [page, setPage] = useState(1);

  // état pour la modale de renommage
  const [projetEnEdition, setProjetEnEdition] = useState(null); // le projet cliqué, ou null
  const [nouveauNom, setNouveauNom] = useState('');
  const [enCours, setEnCours] = useState(false);

  // Fonction de chargement réutilisable : appelée au montage, et après suppression/modification
  const charger = () => {
    setChargement(true);
    obtenirProjetsUtilisateur(obtenirUtilisateurId())
      .then((res) => setProjets(res.data))
      .catch((err) => {
        console.error('Erreur chargement projets', err);
        setErreur('Impossible de charger les projets.');
      })
      .finally(() => setChargement(false));
  };

  useEffect(() => {
    charger();

    // recharge automatiquement quand Header.jsx signale un import réussi
    window.addEventListener('projetImporte', charger);
    return () => window.removeEventListener('projetImporte', charger);
  }, []);

  // --- Suppression ---
  const gererSuppression = async (projet) => {
    const confirme = await demanderConfirmation({
      titre: 'Supprimer ce projet ?',
      message: `Le projet « ${projet.nom} » et ses données associées seront supprimés définitivement.`,
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    supprimerProjet(projet.id)
      .then(() => charger())
      .catch((err) => {
        console.error('Erreur suppression', err);
        afficherAlerte({
          variante: 'error',
          titre: 'Suppression impossible',
          message: 'Le projet n’a pas pu être supprimé.',
          texteConfirmation: 'Fermer',
        });
      });
  };

  // --- Renommage ---
  const ouvrirEdition = (projet) => {
    setProjetEnEdition(projet);
    setNouveauNom(projet.nom);
  };

  const fermerEdition = () => {
    setProjetEnEdition(null);
    setNouveauNom('');
  };

  const gererModification = () => {
    if (!nouveauNom.trim()) return;

    setEnCours(true);
    const projetModifie = { ...projetEnEdition, nom: nouveauNom.trim() };

    modifierProjet(projetEnEdition.id, projetModifie)
      .then(() => {
        fermerEdition();
        charger();
      })
      .catch((err) => {
        console.error('Erreur modification', err);
        afficherAlerte({
          variante: 'error',
          titre: 'Modification impossible',
          message: 'Le nom du projet n’a pas pu être modifié.',
          texteConfirmation: 'Fermer',
        });
      })
      .finally(() => setEnCours(false));
  };

  // 1. Filtrage par recherche (nom du projet)
  let projetsAffiches = projets.filter((p) =>
    p.nom.toLowerCase().includes(recherche.toLowerCase())
  );

  // 2. Filtrage par statut
  if (filtreStatut !== 'Tous') {
    projetsAffiches = projetsAffiches.filter((p) => p.statut === filtreStatut);
  }

  // 3. Tri
  if (tri === 'recent') {
    projetsAffiches = [...projetsAffiches].sort(
      (a, b) => new Date(b.dateImport) - new Date(a.dateImport)
    );
  } else if (tri === 'nom') {
    projetsAffiches = [...projetsAffiches].sort((a, b) => a.nom.localeCompare(b.nom));
  }

  // 4. Pagination
  const totalPages = Math.ceil(projetsAffiches.length / PROJETS_PAR_PAGE) || 1;
  const debut = (page - 1) * PROJETS_PAR_PAGE;
  const projetsPage = projetsAffiches.slice(debut, debut + PROJETS_PAR_PAGE);

  if (page > totalPages) {
    setPage(1);
  }

  return (
    <div className="page">
      {/* --- Barre d'outils --- */}
      <div className="projetsToolbar">
        <div className="projetsSearchWrapper">
          <Search size={18} className="projetsSearchIcon" />
          <input
            type="text"
            placeholder="Rechercher un projet..."
            value={recherche}
            onChange={(e) => setRecherche(e.target.value)}
            className="projetsSearchInput"
          />
        </div>

        <select value={filtreStatut} onChange={(e) => setFiltreStatut(e.target.value)} className="projetsSelect">
          <option value="Tous">Statut: Tous</option>
          <option value="IMPORTE">Importé</option>
          <option value="EN_ERREUR">En erreur</option>
          <option value="SUPPRIME">Supprimé</option>
        </select>

        <select value={tri} onChange={(e) => setTri(e.target.value)} className="projetsSelect">
          <option value="recent">Trier par: Plus récent</option>
          <option value="nom">Trier par: Nom</option>
        </select>
      </div>

      {/* --- Tableau --- */}
      <div className="tableCard">
        <table className="table">
          <thead>
            <tr className="headerRow">
              <th className="th">Projet</th>
              <th className="th">Source</th>
              <th className="th">Statut</th>
              <th className="th">Date d'import</th>
              <th className="th">Actions</th>
            </tr>
          </thead>
          <tbody>
            {chargement && (
              <tr>
                <td colSpan={5} className="emptyState">
                  Chargement des projets...
                </td>
              </tr>
            )}

            {!chargement && erreur && (
              <tr>
                <td colSpan={5} className="errorState">
                  {erreur}
                </td>
              </tr>
            )}

            {!chargement && !erreur && projetsPage.map((projet) => (
              <tr key={projet.id} className="row">
                <td className="td">
                  <div className="projetCell">
                    <div
                      className="avatar"
                      style={{ backgroundColor: PALETTE[projet.id % PALETTE.length] }}
                    >
                      {projet.nom.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div className="projetNom">{projet.nom}</div>
                      {projet.description && (
                        <div className="projetDescription">{projet.description}</div>
                      )}
                    </div>
                  </div>
                </td>
                <td className="td">
                  {projet.typeSource === 'GITHUB' ? 'GitHub' : 'Archive ZIP'}
                </td>
                <td className="td">
                  <span className={`badge ${badgeClasse(projet.statut)}`}>
                    {libelleStatut(projet.statut)}
                  </span>
                </td>
                <td className="td">
                  {projet.dateImport ? new Date(projet.dateImport).toLocaleString('fr-FR') : '-'}
                </td>
                <td className="td">
                  <div className="actions">
                    <Pencil size={18} className="iconEdit" onClick={() => ouvrirEdition(projet)} />
                    <Trash2 size={18} className="iconDelete" onClick={() => gererSuppression(projet)} />
                  </div>
                </td>
              </tr>
            ))}

            {!chargement && !erreur && projetsPage.length === 0 && (
              <tr>
                <td colSpan={5} className="emptyState">
                  Aucun projet ne correspond à ta recherche.
                </td>
              </tr>
            )}
          </tbody>
        </table>

        {/* --- Pied de tableau : compteur + pagination --- */}
        <div className="footer">
          <span className="footerText">
            Affichage de {projetsAffiches.length === 0 ? 0 : debut + 1} à{' '}
            {Math.min(debut + PROJETS_PAR_PAGE, projetsAffiches.length)} sur {projetsAffiches.length} projets
          </span>

          <div className="pagination">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className={`pageBtn ${page === 1 ? 'pageBtnDisabled' : ''}`}
            >
              ‹
            </button>

            {Array.from({ length: totalPages }, (_, i) => i + 1).map((n) => (
              <button
                key={n}
                onClick={() => setPage(n)}
                className={`pageBtn ${n === page ? 'pageBtnActive' : ''}`}
              >
                {n}
              </button>
            ))}

            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className={`pageBtn ${page === totalPages ? 'pageBtnDisabled' : ''}`}
            >
              ›
            </button>
          </div>
        </div>
      </div>

      {/* --- Modale de renommage --- */}
      {projetEnEdition && (
        <div className="modaleOverlay">
          <div className="modaleBox">
            <div className="modaleHeader">
              <h3 className="modaleTitle">Renommer le projet</h3>
              <X size={18} style={{ cursor: 'pointer' }} onClick={fermerEdition} />
            </div>

            <input
              type="text"
              value={nouveauNom}
              onChange={(e) => setNouveauNom(e.target.value)}
              className="modaleInput"
            />

            <button
              onClick={gererModification}
              disabled={enCours || !nouveauNom.trim()}
              className="modaleButton"
            >
              {enCours ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function libelleStatut(statut) {
  const map = { IMPORTE: 'Importé', EN_ERREUR: 'En erreur', SUPPRIME: 'Supprimé' };
  return map[statut] || statut;
}

function badgeClasse(statut) {
  const map = { IMPORTE: 'badgeImporte', EN_ERREUR: 'badgeErreur', SUPPRIME: 'badgeSupprime' };
  return map[statut] || 'badgeImporte';
}

export default Projets;
