import { useEffect, useMemo, useState } from 'react';
import {
  CheckCircle2,
  Eye,
  EyeOff,
  LoaderCircle,
  Pencil,
  Plus,
  Power,
  PowerOff,
  Search,
  Trash2,
  TriangleAlert,
  UserRoundCheck,
  Users,
  X,
} from 'lucide-react';
import { useAlertDialog } from '../../components/AlertDialogContext';
import useAuth from '../../hooks/useAuth';
import {
  changerEtatUtilisateur,
  creerUtilisateur,
  modifierUtilisateur,
  obtenirUtilisateurs,
  supprimerUtilisateur,
} from '../../services/adminUtilisateurService';
import './AdminAccueil.css';
import './AdminUtilisateurs.css';

const FORMULAIRE_VIDE = {
  nom: '',
  email: '',
  motDePasse: '',
  role: 'DEVELOPPEUR',
};

const formaterDate = (date) => {
  if (!date) return '—';

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
  }).format(new Date(date));
};

const extraireMessageErreur = (erreur, messageParDefaut) =>
  erreur.response?.data?.message || messageParDefaut;

function AdminUtilisateurs() {
  const { utilisateur: utilisateurConnecte } = useAuth();
  const { demanderConfirmation } = useAlertDialog();
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [recherche, setRecherche] = useState('');
  const [role, setRole] = useState('TOUS');
  const [etat, setEtat] = useState('TOUS');
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');
  const [modal, setModal] = useState(null);
  const [formulaire, setFormulaire] = useState(FORMULAIRE_VIDE);
  const [motDePasseVisible, setMotDePasseVisible] = useState(false);
  const [soumission, setSoumission] = useState(false);
  const [erreurFormulaire, setErreurFormulaire] = useState('');
  const [actionEnCours, setActionEnCours] = useState(null);
  const [notification, setNotification] = useState(null);

  useEffect(() => {
    let composantActif = true;

    obtenirUtilisateurs()
      .then((donnees) => {
        if (composantActif) setUtilisateurs(donnees);
      })
      .catch((erreurRequete) => {
        if (composantActif) {
          setErreur(extraireMessageErreur(
            erreurRequete,
            'Impossible de charger les utilisateurs.'
          ));
        }
      })
      .finally(() => {
        if (composantActif) setChargement(false);
      });

    return () => {
      composantActif = false;
    };
  }, []);

  const utilisateursFiltres = useMemo(() => {
    const texte = recherche.trim().toLocaleLowerCase('fr');

    return utilisateurs.filter((utilisateur) => {
      const correspondRecherche = !texte
        || `${utilisateur.nom ?? ''} ${utilisateur.email ?? ''}`
          .toLocaleLowerCase('fr')
          .includes(texte);
      const correspondRole = role === 'TOUS' || utilisateur.role === role;
      const correspondEtat = etat === 'TOUS'
        || (etat === 'ACTIF' ? utilisateur.actif : !utilisateur.actif);

      return correspondRecherche && correspondRole && correspondEtat;
    });
  }, [utilisateurs, recherche, role, etat]);

  const estCompteConnecte = (utilisateur) =>
    utilisateur.email.toLowerCase() === utilisateurConnecte?.email?.toLowerCase();

  const ouvrirCreation = () => {
    setFormulaire(FORMULAIRE_VIDE);
    setErreurFormulaire('');
    setMotDePasseVisible(false);
    setModal({ mode: 'creation', utilisateur: null });
  };

  const ouvrirModification = (utilisateur) => {
    setFormulaire({
      nom: utilisateur.nom || '',
      email: utilisateur.email,
      motDePasse: '',
      role: utilisateur.role,
    });
    setErreurFormulaire('');
    setMotDePasseVisible(false);
    setModal({ mode: 'modification', utilisateur });
  };

  const fermerModal = () => {
    if (soumission) return;
    setModal(null);
    setErreurFormulaire('');
  };

  const modifierChamp = (evenement) => {
    const { name, value } = evenement.target;
    setFormulaire((ancien) => ({ ...ancien, [name]: value }));
  };

  const validerFormulaire = () => {
    if (!formulaire.nom.trim() || !formulaire.email.trim() || !formulaire.role) {
      return 'Le nom, l’email et le rôle sont obligatoires.';
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formulaire.email.trim())) {
      return 'L’adresse e-mail n’est pas valide.';
    }

    if (modal.mode === 'creation' && formulaire.motDePasse.length < 8) {
      return 'Le mot de passe doit contenir au moins 8 caractères.';
    }

    return '';
  };

  const soumettreFormulaire = async (evenement) => {
    evenement.preventDefault();
    const messageValidation = validerFormulaire();

    if (messageValidation) {
      setErreurFormulaire(messageValidation);
      return;
    }

    setSoumission(true);
    setErreurFormulaire('');
    setNotification(null);

    try {
      const donneesCommunes = {
        nom: formulaire.nom.trim(),
        email: formulaire.email.trim(),
        role: formulaire.role,
      };

      if (modal.mode === 'creation') {
        const nouvelUtilisateur = await creerUtilisateur({
          ...donneesCommunes,
          motDePasse: formulaire.motDePasse,
        });
        setUtilisateurs((anciens) => [...anciens, nouvelUtilisateur]);
        setNotification({ type: 'succes', message: 'Utilisateur créé avec succès.' });
      } else {
        const utilisateurModifie = await modifierUtilisateur(
          modal.utilisateur.id,
          donneesCommunes
        );
        setUtilisateurs((anciens) => anciens.map((utilisateur) =>
          utilisateur.id === utilisateurModifie.id ? utilisateurModifie : utilisateur
        ));
        setNotification({ type: 'succes', message: 'Utilisateur modifié avec succès.' });
      }

      setModal(null);
    } catch (erreurRequete) {
      setErreurFormulaire(extraireMessageErreur(
        erreurRequete,
        `Impossible de ${modal.mode === 'creation' ? 'créer' : 'modifier'} l’utilisateur.`
      ));
    } finally {
      setSoumission(false);
    }
  };

  const basculerEtat = async (utilisateur) => {
    const nouvelEtat = !utilisateur.actif;
    const confirme = await demanderConfirmation({
      titre: nouvelEtat ? 'Réactiver cet utilisateur ?' : 'Désactiver cet utilisateur ?',
      message: nouvelEtat
        ? `${utilisateur.nom} pourra de nouveau se connecter à la plateforme.`
        : `${utilisateur.nom} ne pourra plus se connecter et ses jetons actuels seront refusés.`,
      texteConfirmation: nouvelEtat ? 'Réactiver' : 'Désactiver',
    });

    if (!confirme) return;

    setActionEnCours(utilisateur.id);
    setNotification(null);

    try {
      const utilisateurActualise = await changerEtatUtilisateur(
        utilisateur.id,
        nouvelEtat
      );
      setUtilisateurs((anciens) => anciens.map((element) =>
        element.id === utilisateurActualise.id ? utilisateurActualise : element
      ));
      setNotification({
        type: 'succes',
        message: nouvelEtat
          ? 'Utilisateur réactivé avec succès.'
          : 'Utilisateur désactivé avec succès.',
      });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de modifier l’état de cet utilisateur.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  const supprimer = async (utilisateur) => {
    const confirme = await demanderConfirmation({
      titre: 'Supprimer définitivement cet utilisateur ?',
      message: `Le compte de ${utilisateur.nom} et ses données associées seront supprimés. Cette action est irréversible.`,
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    setActionEnCours(utilisateur.id);
    setNotification(null);

    try {
      await supprimerUtilisateur(utilisateur.id);
      setUtilisateurs((anciens) => anciens.filter((element) => element.id !== utilisateur.id));
      setNotification({ type: 'succes', message: 'Utilisateur supprimé avec succès.' });
    } catch (erreurRequete) {
      setNotification({
        type: 'erreur',
        message: extraireMessageErreur(
          erreurRequete,
          'Impossible de supprimer cet utilisateur.'
        ),
      });
    } finally {
      setActionEnCours(null);
    }
  };

  return (
    <section className="admin-users-content">
      <div className="admin-users-toolbar">
        <div className="admin-users-title-actions">
          <div className="admin-users-count">
            <Users size={19} />
            <strong>{utilisateursFiltres.length}</strong>
            <span>résultat{utilisateursFiltres.length > 1 ? 's' : ''}</span>
          </div>
          <button type="button" className="admin-create-user" onClick={ouvrirCreation}>
            <Plus size={18} /> Créer un utilisateur
          </button>
        </div>
      </div>

      {notification && (
        <div className={`admin-users-notification is-${notification.type}`} role="status">
          {notification.type === 'succes'
            ? <CheckCircle2 size={19} />
            : <TriangleAlert size={19} />}
          <span>{notification.message}</span>
          <button type="button" aria-label="Fermer" onClick={() => setNotification(null)}>
            <X size={16} />
          </button>
        </div>
      )}

      <section className="admin-users-filters" aria-label="Filtres des utilisateurs">
        <label className="admin-search-field">
          <Search size={18} />
          <input
            type="search"
            value={recherche}
            onChange={(evenement) => setRecherche(evenement.target.value)}
            placeholder="Rechercher par nom ou email..."
          />
        </label>

        <label>
          <span>Rôle</span>
          <select value={role} onChange={(evenement) => setRole(evenement.target.value)}>
            <option value="TOUS">Tous les rôles</option>
            <option value="ADMIN">Administrateur</option>
            <option value="DEVELOPPEUR">Développeur</option>
          </select>
        </label>

        <label>
          <span>État</span>
          <select value={etat} onChange={(evenement) => setEtat(evenement.target.value)}>
            <option value="TOUS">Tous les états</option>
            <option value="ACTIF">Actif</option>
            <option value="INACTIF">Inactif</option>
          </select>
        </label>
      </section>

      {chargement && (
        <div className="admin-users-feedback">
          <LoaderCircle className="admin-spin" size={27} />
          Chargement des utilisateurs...
        </div>
      )}

      {erreur && !chargement && (
        <div className="admin-users-feedback admin-users-error">
          <TriangleAlert size={25} />
          <strong>Le chargement a échoué</strong>
          <span>{erreur}</span>
        </div>
      )}

      {!chargement && !erreur && (
        <section className="admin-users-table-card">
          {utilisateursFiltres.length === 0 ? (
            <div className="admin-users-empty">
              <UserRoundCheck size={31} />
              <strong>Aucun utilisateur trouvé</strong>
              <span>Essayez de modifier les critères de recherche.</span>
            </div>
          ) : (
            <div className="admin-users-table-scroll">
              <table className="admin-users-table">
                <thead>
                  <tr>
                    <th>Utilisateur</th>
                    <th>Rôle</th>
                    <th>État</th>
                    <th>Date de création</th>
                    <th className="admin-actions-heading">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {utilisateursFiltres.map((utilisateur) => {
                    const compteConnecte = estCompteConnecte(utilisateur);
                    const traitement = actionEnCours === utilisateur.id;

                    return (
                      <tr key={utilisateur.id}>
                        <td>
                          <div className="admin-user-identity">
                            <span className="admin-user-avatar">
                              {(utilisateur.nom || utilisateur.email).charAt(0).toUpperCase()}
                            </span>
                            <div>
                              <strong>
                                {utilisateur.nom || 'Sans nom'}
                                {compteConnecte && <small>Vous</small>}
                              </strong>
                              <span>{utilisateur.email}</span>
                            </div>
                          </div>
                        </td>
                        <td>
                          <span className={`admin-role-badge admin-role-${utilisateur.role.toLowerCase()}`}>
                            {utilisateur.role === 'ADMIN' ? 'Administrateur' : 'Développeur'}
                          </span>
                        </td>
                        <td>
                          <span className={`admin-user-state ${utilisateur.actif ? 'is-active' : 'is-inactive'}`}>
                            <i /> {utilisateur.actif ? 'Actif' : 'Inactif'}
                          </span>
                        </td>
                        <td><time>{formaterDate(utilisateur.dateCreation)}</time></td>
                        <td>
                          <div className="admin-user-actions">
                            <button
                              type="button"
                              className="is-edit"
                              title="Modifier"
                              aria-label={`Modifier ${utilisateur.nom}`}
                              onClick={() => ouvrirModification(utilisateur)}
                              disabled={traitement}
                            >
                              <Pencil size={15} />
                            </button>
                            <button
                              type="button"
                              className={utilisateur.actif ? 'is-disable' : 'is-enable'}
                              title={compteConnecte ? 'Action impossible sur votre compte' : utilisateur.actif ? 'Désactiver' : 'Réactiver'}
                              aria-label={`${utilisateur.actif ? 'Désactiver' : 'Réactiver'} ${utilisateur.nom}`}
                              onClick={() => basculerEtat(utilisateur)}
                              disabled={traitement || compteConnecte}
                            >
                              {traitement
                                ? <LoaderCircle size={15} className="admin-spin" />
                                : utilisateur.actif ? <PowerOff size={15} /> : <Power size={15} />}
                            </button>
                            <button
                              type="button"
                              className="is-delete"
                              title={compteConnecte ? 'Vous ne pouvez pas supprimer votre compte' : 'Supprimer'}
                              aria-label={`Supprimer ${utilisateur.nom}`}
                              onClick={() => supprimer(utilisateur)}
                              disabled={traitement || compteConnecte}
                            >
                              <Trash2 size={15} />
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

      {modal && (
        <div
          className="admin-user-modal-overlay"
          role="presentation"
          onMouseDown={(evenement) => {
            if (evenement.target === evenement.currentTarget) fermerModal();
          }}
        >
          <section
            className="admin-user-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="admin-user-modal-title"
          >
            <div className="admin-user-modal-header">
              <div>
                <span>{modal.mode === 'creation' ? 'Nouveau compte' : 'Modification'}</span>
                <h2 id="admin-user-modal-title">
                  {modal.mode === 'creation' ? 'Créer un utilisateur' : 'Modifier l’utilisateur'}
                </h2>
              </div>
              <button type="button" aria-label="Fermer" onClick={fermerModal} disabled={soumission}>
                <X size={19} />
              </button>
            </div>

            <form onSubmit={soumettreFormulaire}>
              {erreurFormulaire && (
                <div className="admin-user-form-error" role="alert">
                  <TriangleAlert size={17} /> {erreurFormulaire}
                </div>
              )}

              <label>
                <span>Nom complet</span>
                <input
                  name="nom"
                  value={formulaire.nom}
                  onChange={modifierChamp}
                  placeholder="Ex. Sara Mansouri"
                  autoComplete="name"
                  disabled={soumission}
                  required
                />
              </label>

              <label>
                <span>Adresse email</span>
                <input
                  type="email"
                  name="email"
                  value={formulaire.email}
                  onChange={modifierChamp}
                  placeholder="nom@example.com"
                  autoComplete="email"
                  disabled={soumission || (modal.mode === 'modification' && estCompteConnecte(modal.utilisateur))}
                  required
                />
                {modal.mode === 'modification' && estCompteConnecte(modal.utilisateur) && (
                  <small>Modifiez votre propre email depuis votre profil afin de préserver votre session.</small>
                )}
              </label>

              {modal.mode === 'creation' && (
                <label>
                  <span>Mot de passe</span>
                  <div className="admin-password-field">
                    <input
                      type={motDePasseVisible ? 'text' : 'password'}
                      name="motDePasse"
                      value={formulaire.motDePasse}
                      onChange={modifierChamp}
                      placeholder="8 caractères minimum"
                      autoComplete="new-password"
                      minLength={8}
                      disabled={soumission}
                      required
                    />
                    <button
                      type="button"
                      aria-label={motDePasseVisible ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                      onClick={() => setMotDePasseVisible((visible) => !visible)}
                    >
                      {motDePasseVisible ? <EyeOff size={17} /> : <Eye size={17} />}
                    </button>
                  </div>
                </label>
              )}

              <label>
                <span>Rôle</span>
                <select
                  name="role"
                  value={formulaire.role}
                  onChange={modifierChamp}
                  disabled={soumission || (modal.mode === 'modification' && estCompteConnecte(modal.utilisateur))}
                >
                  <option value="DEVELOPPEUR">Développeur</option>
                  <option value="ADMIN">Administrateur</option>
                </select>
                {modal.mode === 'modification' && estCompteConnecte(modal.utilisateur) && (
                  <small>Vous ne pouvez pas retirer votre propre rôle administrateur.</small>
                )}
              </label>

              <div className="admin-user-modal-actions">
                <button type="button" className="is-cancel" onClick={fermerModal} disabled={soumission}>
                  Annuler
                </button>
                <button type="submit" className="is-submit" disabled={soumission}>
                  {soumission && <LoaderCircle size={16} className="admin-spin" />}
                  {modal.mode === 'creation' ? 'Créer le compte' : 'Enregistrer'}
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </section>
  );
}

export default AdminUtilisateurs;
