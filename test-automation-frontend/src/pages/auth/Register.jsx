import { useState } from 'react';
import {
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  ShieldCheck,
  UserRound,
} from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import { inscrire } from '../../services/authService';
import illustration from '../../assets/logo.png';
import './Login.css';

function Register() {
  const navigate = useNavigate();
  const { connecter } = useAuth();
  const [formulaire, setFormulaire] = useState({
    nom: '',
    email: '',
    motDePasse: '',
    confirmation: '',
  });
  const [motDePasseVisible, setMotDePasseVisible] = useState(false);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState('');

  const gererChangement = (event) => {
    const { name, value } = event.target;
    setFormulaire((ancien) => ({ ...ancien, [name]: value }));
    setErreur('');
  };

  const gererInscription = async (event) => {
    event.preventDefault();
    const nom = formulaire.nom.trim();
    const email = formulaire.email.trim();

    if (!nom || !email || !formulaire.motDePasse || !formulaire.confirmation) {
      setErreur('Tous les champs sont obligatoires.');
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(email)) {
      setErreur('Saisissez une adresse e-mail valide.');
      return;
    }

    if (formulaire.motDePasse.length < 8) {
      setErreur('Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }

    if (formulaire.motDePasse !== formulaire.confirmation) {
      setErreur('Les deux mots de passe ne correspondent pas.');
      return;
    }

    setChargement(true);
    setErreur('');

    try {
      await inscrire(nom, email, formulaire.motDePasse);
      await connecter(email, formulaire.motDePasse);
      navigate('/', { replace: true });
    } catch (error) {
      if (!error.response) {
        setErreur('Le serveur est inaccessible. Vérifiez que le backend est démarré.');
      } else {
        setErreur(error.response.data?.message || 'Inscription impossible.');
      }
    } finally {
      setChargement(false);
    }
  };

  return (
    <main className="login-page register-page">
      <section className="login-presentation" aria-label="Présentation de la plateforme">
        <div className="login-brand">
          <span className="login-brand-icon"><ShieldCheck size={24} /></span>
          <span>Test Automation Platform</span>
        </div>

        <div className="login-presentation-content">
          <img src={illustration} alt="Robot de tests automatisés" />
          <p className="login-eyebrow">Votre espace développeur</p>
          <h1>Commencez à tester<br />en quelques instants.</h1>
          <p>
            Créez votre compte pour centraliser vos projets et suivre toutes
            vos exécutions de tests dans un environnement sécurisé.
          </p>
        </div>

        <p className="login-copyright">© 2026 Test Automation Platform</p>
      </section>

      <section className="login-form-section register-form-section">
        <div className="login-card register-card">
          <div className="login-mobile-brand">
            <ShieldCheck size={25} />
            <span>Test Automation Platform</span>
          </div>

          <p className="login-kicker">Nouveau compte</p>
          <h2>Créez votre espace</h2>
          <p className="login-subtitle">
            Votre compte sera créé avec le rôle développeur.
          </p>

          <form onSubmit={gererInscription} noValidate>
            <label htmlFor="nom">Nom complet</label>
            <div className="login-input-wrapper">
              <UserRound size={19} />
              <input
                id="nom"
                name="nom"
                type="text"
                value={formulaire.nom}
                onChange={gererChangement}
                placeholder="Votre nom complet"
                autoComplete="name"
                autoFocus
                disabled={chargement}
              />
            </div>

            <label htmlFor="email">Adresse e-mail</label>
            <div className="login-input-wrapper">
              <Mail size={19} />
              <input
                id="email"
                name="email"
                type="email"
                value={formulaire.email}
                onChange={gererChangement}
                placeholder="nom@entreprise.com"
                autoComplete="email"
                disabled={chargement}
              />
            </div>

            <label htmlFor="motDePasse">Mot de passe</label>
            <div className="login-input-wrapper">
              <LockKeyhole size={19} />
              <input
                id="motDePasse"
                name="motDePasse"
                type={motDePasseVisible ? 'text' : 'password'}
                value={formulaire.motDePasse}
                onChange={gererChangement}
                placeholder="8 caractères minimum"
                autoComplete="new-password"
                disabled={chargement}
              />
              <button
                className="login-password-toggle"
                type="button"
                aria-label={motDePasseVisible ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                onClick={() => setMotDePasseVisible((visible) => !visible)}
              >
                {motDePasseVisible ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>

            <label htmlFor="confirmation">Confirmer le mot de passe</label>
            <div className="login-input-wrapper">
              <LockKeyhole size={19} />
              <input
                id="confirmation"
                name="confirmation"
                type={motDePasseVisible ? 'text' : 'password'}
                value={formulaire.confirmation}
                onChange={gererChangement}
                placeholder="Répétez le mot de passe"
                autoComplete="new-password"
                disabled={chargement}
              />
            </div>

            {erreur && <div className="login-error" role="alert">{erreur}</div>}

            <button className="login-submit" type="submit" disabled={chargement}>
              {chargement ? (
                <><LoaderCircle className="login-spinner" size={19} /> Création...</>
              ) : (
                'Créer mon compte'
              )}
            </button>
          </form>

          <p className="login-help auth-switch-link">
            Vous avez déjà un compte ? <Link to="/login">Se connecter</Link>
          </p>
        </div>
      </section>
    </main>
  );
}

export default Register;
