import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, Folder, Settings, Play, BarChart2,
  ShieldCheck, User, ArrowLeft,
} from 'lucide-react';
import useAuth from '../hooks/useAuth';

function Sidebar() {
  const location = useLocation();
  const { utilisateur } = useAuth();
  const [survole, setSurvole] = useState(null);

  const liens = [
    { texte: 'Tableau de bord', chemin: '/', icone: LayoutDashboard },
    { texte: 'Mes projets', chemin: '/projets', icone: Folder },
    { texte: 'Configurations de tests', chemin: '/configurations', icone: Settings },
    { texte: 'Exécutions', chemin: '/executions', icone: Play },
    { texte: 'Résultats des tests', chemin: '/resultats', icone: BarChart2 },
    { texte: 'Analyse de qualité', chemin: '/qualite', icone: ShieldCheck },
    { texte: 'Mon profil', chemin: '/profil', icone: User },
  ];

  return (
    <aside
      style={{
        width: '260px',
        flex: '0 0 260px',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: '#ffffff',
        boxSizing: 'border-box',
      }}
    >
      <div
        style={{
          padding: '14px 20px',
          borderBottom: '1px solid #e8eef3',
        }}
      >
        <h1
          style={{
            fontSize: '15px',
            fontWeight: '700',
            color: '#111827',
            margin: 0,
            lineHeight: '1.25',
          }}
        >
          Test Automation<br />Platform
        </h1>
      </div>

      <nav style={{ paddingTop: '12px', flex: 1 }}>
        {liens.map((lien) => {
          const Icone = lien.icone;
          const estActif = location.pathname === lien.chemin;
          const estSurvole = survole === lien.chemin;

          return (
            <Link
              key={lien.chemin}
              to={lien.chemin}
              onMouseEnter={() => setSurvole(lien.chemin)}
              onMouseLeave={() => setSurvole(null)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '12px 20px',
                margin: '0 10px',
                borderRadius: '8px',
                color: estActif ? '#0284c7' : '#475569',
                backgroundColor: estActif
                  ? '#e0f2fe'
                  : estSurvole
                    ? '#f1f5f9'
                    : 'transparent',
                textDecoration: 'none',
                fontWeight: estActif ? 'bold' : 'normal',
              }}
            >
              <Icone size={18} />
              {lien.texte}
            </Link>
          );
        })}
      </nav>

      {utilisateur?.role === 'ADMIN' && (
        <div
          style={{
            padding: '12px 10px',
            borderTop: '1px solid #e8eef3',
          }}
        >
          <Link
            to="/admin"
            onMouseEnter={() => setSurvole('/admin')}
            onMouseLeave={() => setSurvole(null)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              padding: '11px 10px',
              color: survole === '/admin' ? '#0284c7' : '#475569',
              borderRadius: '8px',
              backgroundColor: survole === '/admin' ? '#e0f2fe' : 'transparent',
              textDecoration: 'none',
              fontSize: '13px',
            }}
          >
            <ArrowLeft size={18} />
            <span>Retour administration</span>
          </Link>
        </div>
      )}
    </aside>
  );
}

export default Sidebar;
