import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, Folder, Settings, Play, BarChart2,
  ShieldCheck, FileText, History, Bell, User,
} from 'lucide-react';

function Sidebar() {
  const location = useLocation();
  const [survole, setSurvole] = useState(null);

  const liens = [
    { texte: 'Tableau de bord', chemin: '/', icone: LayoutDashboard },
    { texte: 'Mes projets', chemin: '/projets', icone: Folder },
    { texte: 'Configurations de tests', chemin: '/configurations', icone: Settings },
    { texte: 'Exécutions', chemin: '/executions', icone: Play },
    { texte: 'Résultats des tests', chemin: '/resultats', icone: BarChart2 },
    { texte: 'Analyse de qualité', chemin: '/qualite', icone: ShieldCheck },
    { texte: 'Rapports', chemin: '/rapports', icone: FileText },
    { texte: 'Historique', chemin: '/historique', icone: History },
    { texte: 'Notifications', chemin: '/notifications', icone: Bell },
    { texte: 'Mon profil', chemin: '/profil', icone: User },
  ];

  return (
    <aside style={{ width: '260px', height: '100vh', backgroundColor: '#ffffff', borderRight: '1px solid #e5e7eb' }}>
      <div style={{ padding: '24px 20px', borderBottom: '1px solid #f3f4f6' }}>
  <h1 style={{ fontSize: '17px', fontWeight: '700', color: '#111827', margin: 0, lineHeight: '1.3' }}>
    Test Automation<br />Platform
  </h1>
</div>

      <nav>
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
                color: estActif ? '#7c3aed' : '#374151',
                backgroundColor: estActif ? '#f3e8ff' : estSurvole ? '#f9fafb' : 'transparent',
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
    </aside>
  );
}

export default Sidebar;