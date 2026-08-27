import { ArrowRight, LogOut, ShieldCheck } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import './AdminAccueil.css';

function AdminAccueil() {
  const navigate = useNavigate();
  const { utilisateur, deconnecter } = useAuth();

  const gererDeconnexion = () => {
    deconnecter();
    navigate('/login', { replace: true });
  };

  return (
    <main className="admin-welcome-page">
      <header className="admin-welcome-header">
        <div className="admin-welcome-brand">
          <span><ShieldCheck size={22} /></span>
          Test Automation Platform
        </div>
        <button type="button" onClick={gererDeconnexion}>
          <LogOut size={17} /> Déconnexion
        </button>
      </header>

      <section className="admin-welcome-card">
        <span className="admin-welcome-badge">Session administrateur</span>
        <h1>Bienvenue, {utilisateur.nom}</h1>
        <p>
          Votre connexion est sécurisée. Le prochain travail sera la construction
          des fonctionnalités de gestion de l’espace administrateur.
        </p>
        <button type="button" onClick={() => navigate('/')}>
          Accéder aux fonctions développeur <ArrowRight size={18} />
        </button>
      </section>
    </main>
  );
}

export default AdminAccueil;
