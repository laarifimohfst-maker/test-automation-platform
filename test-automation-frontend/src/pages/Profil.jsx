import { useEffect, useMemo, useState } from 'react';
import {
  CalendarDays,
  Mail,
  RotateCcw,
  Save,
  ShieldCheck,
  UserRound,
} from 'lucide-react';

import {
  modifierUtilisateur,
  obtenirUtilisateurParId,
} from '../services/utilisateurService';

import './Profil.css';

const UTILISATEUR_ID = 1;

function Profil() {
  const [utilisateur, setUtilisateur] = useState(null);
  const [formulaire, setFormulaire] = useState({ nom: '', email: '' });
  const [chargement, setChargement] = useState(true);
  const [enregistrement, setEnregistrement] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [succes, setSucces] = useState(null);

  useEffect(() => {
    let composantActif = true;

    obtenirUtilisateurParId(UTILISATEUR_ID)
      .then((response) => {
        if (!composantActif) return;

        setUtilisateur(response.data);
        setFormulaire({
          nom: response.data.nom || '',
          email: response.data.email || '',
        });
      })
      .catch((err) => {
        console.error('Erreur chargement profil :', err);
        if (composantActif) {
          setErreur('Impossible de charger votre profil.');
        }
      })
      .finally(() => {
        if (composantActif) setChargement(false);
      });

    return () => {
      composantActif = false;
    };
  }, []);

  const initiales = useMemo(() => {
    const morceaux = formulaire.nom.trim().split(/\s+/).filter(Boolean);

    if (morceaux.length === 0) return 'U';

    return morceaux
      .slice(0, 2)
      .map((morceau) => morceau.charAt(0).toUpperCase())
      .join('');
  }, [formulaire.nom]);

  const roleLibelle = (role) => {
    if (role === 'ADMIN') return 'Administrateur';
    if (role === 'DEVELOPPEUR') return 'Développeur';
    return role || 'Non renseigné';
  };

  const formaterDate = (date) => {
    if (!date) return 'Non renseignée';
    return new Date(date).toLocaleDateString('fr-FR');
  };

  const gererChangement = (event) => {
    const { name, value } = event.target;

    setFormulaire((ancien) => ({ ...ancien, [name]: value }));
    setErreur(null);
    setSucces(null);
  };

  const annulerModifications = () => {
    if (!utilisateur) return;

    setFormulaire({
      nom: utilisateur.nom || '',
      email: utilisateur.email || '',
    });
    setErreur(null);
    setSucces(null);
  };

  const gererEnregistrement = async (event) => {
    event.preventDefault();

    const nom = formulaire.nom.trim();
    const email = formulaire.email.trim();

    if (!nom) {
      setErreur('Le nom est obligatoire.');
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(email)) {
      setErreur('Saisissez une adresse e-mail valide.');
      return;
    }

    setEnregistrement(true);
    setErreur(null);
    setSucces(null);

    try {
      const response = await modifierUtilisateur(UTILISATEUR_ID, {
        nom,
        email,
        role: utilisateur.role,
      });

      setUtilisateur(response.data);
      setFormulaire({
        nom: response.data.nom || '',
        email: response.data.email || '',
      });
      setSucces('Votre profil a été mis à jour avec succès.');

      window.dispatchEvent(
        new CustomEvent('profilMisAJour', { detail: response.data })
      );
    } catch (err) {
      console.error('Erreur modification profil :', err);
      setErreur(
        err.response?.data?.message ||
          'Impossible de mettre à jour votre profil.'
      );
    } finally {
      setEnregistrement(false);
    }
  };

  if (chargement) {
    return <div className="profile-state">Chargement du profil...</div>;
  }

  if (!utilisateur) {
    return <div className="profile-state profile-state-error">{erreur}</div>;
  }

  const modificationsPresentes =
    formulaire.nom.trim() !== (utilisateur.nom || '') ||
    formulaire.email.trim() !== (utilisateur.email || '');

  return (
    <div className="profile-page">
      <aside className="profile-summary-card">
        <div className="profile-avatar">{initiales}</div>

        <h3>{utilisateur.nom}</h3>
        <p>{utilisateur.email}</p>

        <span className="profile-role-badge">
          <ShieldCheck size={15} />
          {roleLibelle(utilisateur.role)}
        </span>

        <div className="profile-summary-details">
          <div>
            <CalendarDays size={17} />
            <span>
              <small>Membre depuis</small>
              <strong>{formaterDate(utilisateur.dateCreation)}</strong>
            </span>
          </div>

          <div>
            <ShieldCheck size={17} />
            <span>
              <small>Identifiant</small>
              <strong>#{utilisateur.id}</strong>
            </span>
          </div>
        </div>
      </aside>

      <section className="profile-form-card">
        <div className="profile-card-header">
          <div>
            <h3>Informations personnelles</h3>
            <p>Modifiez les informations associées à votre compte.</p>
          </div>
        </div>

        <form onSubmit={gererEnregistrement}>
          <div className="profile-form-grid">
            <label className="profile-field">
              <span>Nom complet</span>
              <div className="profile-input-wrapper">
                <UserRound size={18} />
                <input
                  type="text"
                  name="nom"
                  value={formulaire.nom}
                  onChange={gererChangement}
                  autoComplete="name"
                  maxLength={100}
                />
              </div>
            </label>

            <label className="profile-field">
              <span>Adresse e-mail</span>
              <div className="profile-input-wrapper">
                <Mail size={18} />
                <input
                  type="email"
                  name="email"
                  value={formulaire.email}
                  onChange={gererChangement}
                  autoComplete="email"
                  maxLength={150}
                />
              </div>
            </label>

            <label className="profile-field">
              <span>Rôle</span>
              <div className="profile-input-wrapper profile-input-readonly">
                <ShieldCheck size={18} />
                <input
                  type="text"
                  value={roleLibelle(utilisateur.role)}
                  readOnly
                />
              </div>
              <small>Le rôle est géré par l’administration.</small>
            </label>
          </div>

          {erreur && <div className="profile-message profile-error">{erreur}</div>}
          {succes && <div className="profile-message profile-success">{succes}</div>}

          <div className="profile-actions">
            <button
              type="button"
              className="profile-cancel-button"
              disabled={!modificationsPresentes || enregistrement}
              onClick={annulerModifications}
            >
              <RotateCcw size={16} />
              Annuler
            </button>

            <button
              type="submit"
              className="profile-save-button"
              disabled={!modificationsPresentes || enregistrement}
            >
              <Save size={16} />
              {enregistrement ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

export default Profil;
