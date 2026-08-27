import {
  ArrowRight,
  FileText,
  Folder,
  LayoutDashboard,
  LogOut,
  Play,
  Users,
} from 'lucide-react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';
import NotificationBell from '../NotificationBell';
import './AdminLayout.css';

const liensActifs = [
  { chemin: '/admin', libelle: 'Tableau de bord', icone: LayoutDashboard, fin: true },
  { chemin: '/admin/utilisateurs', libelle: 'Gestion utilisateurs', icone: Users },
];

const liensAVenir = [
  { libelle: 'Gestion projets', icone: Folder },
  { libelle: 'Gestion exécutions', icone: Play },
  { libelle: 'Gestion rapports', icone: FileText },
];

const informationsPages = {
  '/admin': {
    titre: 'Bonjour 👋',
    description: 'Voici un aperçu global de la plateforme.',
  },
  '/admin/utilisateurs': {
    titre: 'Gestion des utilisateurs',
    description: 'Créez et gérez les comptes enregistrés sur la plateforme.',
  },
};

function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { utilisateur, deconnecter } = useAuth();
  const page = informationsPages[location.pathname] || {
    titre: 'Administration',
    description: 'Gérez les ressources de la plateforme.',
  };

  const initiales = (utilisateur?.nom || utilisateur?.email || 'A')
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((partie) => partie.charAt(0).toUpperCase())
    .join('');

  const gererDeconnexion = () => {
    deconnecter();
    navigate('/login', { replace: true });
  };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-brand">
          <h1>Test Automation<br />Platform</h1>
          <span>Administration</span>
        </div>

        <nav className="admin-sidebar-nav" aria-label="Navigation administrateur">
          {liensActifs.map(({ chemin, libelle, icone: Icone, fin }) => (
            <NavLink
              key={chemin}
              to={chemin}
              end={fin}
              className={({ isActive }) => `admin-sidebar-link${isActive ? ' is-active' : ''}`}
            >
              <Icone size={18} />
              <span>{libelle}</span>
            </NavLink>
          ))}

          {liensAVenir.map(({ libelle, icone: Icone }) => (
            <button key={libelle} type="button" className="admin-sidebar-link is-disabled" disabled>
              <Icone size={18} />
              <span>{libelle}</span>
              <small>À venir</small>
            </button>
          ))}
        </nav>

        <div className="admin-sidebar-footer">
          <button type="button" onClick={() => navigate('/')}>
            <ArrowRight size={18} />
            <span>Espace développeur</span>
          </button>
        </div>
      </aside>

      <div className="admin-layout-content">
        <header className="admin-header">
          <div>
            <h2>{page.titre}</h2>
            <p>{page.description}</p>
          </div>

          <div className="admin-header-actions">
            <NotificationBell />
            <button
              type="button"
              className="admin-header-profile"
              aria-label="Ouvrir mon profil"
              title={utilisateur?.nom || 'Administrateur'}
              onClick={() => navigate('/profil')}
            >
              {initiales}
            </button>
            <button
              type="button"
              className="admin-header-logout"
              aria-label="Se déconnecter"
              title="Se déconnecter"
              onClick={gererDeconnexion}
            >
              <LogOut size={19} />
            </button>
          </div>
        </header>

        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AdminLayout;
