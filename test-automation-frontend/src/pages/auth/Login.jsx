import { useState } from 'react';
import { Eye, EyeOff, LoaderCircle, LockKeyhole, Mail, ShieldCheck } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import illustration from '../../assets/logo.png';
import './Login.css';

function Login() {
  const navigate = useNavigate();
  const { connecter } = useAuth();
  const [email, setEmail] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [motDePasseVisible, setMotDePasseVisible] = useState(false);
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState('');

  const gererConnexion = async (event) => {
    event.preventDefault();
    setErreur('');

    if (!email.trim() || !motDePasse) {
      setErreur("L'adresse e-mail et le mot de passe sont obligatoires.");
      return;
    }

    setChargement(true);

    try {
      const utilisateur = await connecter(email.trim(), motDePasse);
      navigate(utilisateur.role === 'ADMIN' ? '/admin' : '/', { replace: true });
    } catch (error) {
      if (!error.response) {
        setErreur('Le serveur est inaccessible. Vérifiez que le backend est démarré.');
      } else {
        setErreur(error.response.data?.message || 'Connexion impossible.');
      }
    } finally {
      setChargement(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-presentation" aria-label="Présentation de la plateforme">
        <div className="login-brand">
          <span className="login-brand-icon"><ShieldCheck size={24} /></span>
          <span>Test Automation Platform</span>
        </div>

        <div className="login-presentation-content">
          <img src={illustration} alt="Robot de tests automatisés" />
          <p className="login-eyebrow">Qualité logicielle simplifiée</p>
          <h1>Testez. Analysez.<br />Livrez en confiance.</h1>
          <p>
            Centralisez vos projets, exécutez vos tests et suivez la qualité
            de vos applications depuis un espace sécurisé.
          </p>
        </div>

        <p className="login-copyright">© 2026 Test Automation Platform</p>
      </section>

      <section className="login-form-section">
        <div className="login-card">
          <div className="login-mobile-brand">
            <ShieldCheck size={25} />
            <span>Test Automation Platform</span>
          </div>

          <p className="login-kicker">Bienvenue</p>
          <h2>Connectez-vous à votre espace</h2>
          <p className="login-subtitle">
            Utilisez les identifiants associés à votre compte.
          </p>

          <form onSubmit={gererConnexion} noValidate>
            <label htmlFor="email">Adresse e-mail</label>
            <div className="login-input-wrapper">
              <Mail size={19} />
              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="nom@entreprise.com"
                autoComplete="email"
                autoFocus
                disabled={chargement}
              />
            </div>

            <label htmlFor="motDePasse">Mot de passe</label>
            <div className="login-input-wrapper">
              <LockKeyhole size={19} />
              <input
                id="motDePasse"
                type={motDePasseVisible ? 'text' : 'password'}
                value={motDePasse}
                onChange={(event) => setMotDePasse(event.target.value)}
                placeholder="Votre mot de passe"
                autoComplete="current-password"
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

            {erreur && <div className="login-error" role="alert">{erreur}</div>}

            <button className="login-submit" type="submit" disabled={chargement}>
              {chargement ? (
                <><LoaderCircle className="login-spinner" size={19} /> Connexion...</>
              ) : (
                'Se connecter'
              )}
            </button>
          </form>

          <p className="login-help auth-switch-link">
            Vous n’avez pas encore de compte ? <Link to="/register">Créer un compte</Link>
          </p>
        </div>
      </section>
    </main>
  );
}

export default Login;
