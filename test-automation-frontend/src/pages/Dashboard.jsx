import { useState, useEffect } from 'react';
import { obtenirDashboard } from '../services/dashboardService';
import StatCard from '../components/StatCard';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { Folder, Play, CheckCircle, XCircle, AlertCircle, Shield } from 'lucide-react';

function Dashboard() {
  const [donnees, setDonnees] = useState(null);

  useEffect(() => {
    obtenirDashboard(1).then((resultat) => {
      setDonnees(resultat);
    });
  }, []);

  if (!donnees) {
    return <p>Chargement...</p>;
  }

  const repartitionTests = [
    { nom: 'Réussis', valeur: donnees.testsReussis, couleur: '#16a34a' },
    { nom: 'Échoués', valeur: donnees.testsEchoues, couleur: '#dc2626' },
    { nom: 'Ignorés', valeur: donnees.testsIgnores, couleur: '#d97706' },
  ];

  const qualityGate = [
    { nom: 'Réussis', valeur: donnees.gatesOk, couleur: '#16a34a' },
    { nom: 'Échoués', valeur: donnees.gatesKo, couleur: '#dc2626' },
  ];

  const graphiquesDonut = [
    { titre: 'Répartition des tests', data: repartitionTests },
    { titre: 'Quality Gate', data: qualityGate },
  ];

  return (
    <div style={{ paddingBottom: '16px' }}>
      <div style={{ display: 'flex', gap: '12px', marginTop: '14px' }}>
        <StatCard titre="Projets" valeur={donnees.nbProjets} couleur="#7c3aed" fondIcone="#ede9fe" icone={<Folder size={20} />} />
        <StatCard titre="Exécutions" valeur={donnees.nbExecutions} couleur="#16a34a" fondIcone="#dcfce7" icone={<Play size={20} />} />
        <StatCard titre="Tests réussis" valeur={donnees.testsReussis} couleur="#16a34a" fondIcone="#dcfce7" icone={<CheckCircle size={20} />} />
        <StatCard titre="Tests échoués" valeur={donnees.testsEchoues} couleur="#dc2626" fondIcone="#fee2e2" icone={<XCircle size={20} />} />
        <StatCard titre="Quality Gates OK" valeur={donnees.gatesOk} couleur="#d97706" fondIcone="#fef3c7" icone={<AlertCircle size={20} />} />
        <StatCard titre="Quality Gates KO" valeur={donnees.gatesKo} couleur="#7c3aed" fondIcone="#ede9fe" icone={<Shield size={20} />} />
      </div>

      <div style={{ display: 'flex', gap: '12px', marginTop: '14px' }}>
        <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', padding: '12px', flex: 2 }}>
          <p style={{ margin: '0 0 10px 0', fontWeight: 'bold' }}>Évolution des exécutions</p>
          <ResponsiveContainer width="100%" height={180}>
            <LineChart data={donnees.evolutionExecutions}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Line type="monotone" dataKey="total" stroke="#7c3aed" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {graphiquesDonut.map((graphique) => {
          const total = graphique.data.reduce((somme, entree) => somme + entree.valeur, 0);

          return (
            <div key={graphique.titre} style={{ backgroundColor: '#ffffff', borderRadius: '10px', padding: '12px', flex: 1 }}>
              <p style={{ margin: '0 0 10px 0', fontWeight: 'bold' }}>{graphique.titre}</p>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', height: '120px' }}>
                <ResponsiveContainer width={110} height={110}>
                  <PieChart>
                    <Pie data={graphique.data} dataKey="valeur" nameKey="nom" innerRadius={30} outerRadius={50}>
                      {graphique.data.map((entree) => (
                        <Cell key={entree.nom} fill={entree.couleur} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>

                <div>
                  {graphique.data.map((entree) => (
                    <div key={entree.nom} style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                      <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: entree.couleur, display: 'inline-block' }}></span>
                      <span style={{ fontSize: '12px' }}>
                        {entree.nom} ({entree.valeur}) {total > 0 ? Math.round((entree.valeur / total) * 1000) / 10 : 0}%
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ display: 'flex', gap: '12px', marginTop: '14px' }}>
        <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', padding: '12px', flex: 1 }}>
          <p style={{ margin: '0 0 10px 0', fontWeight: 'bold' }}>Dernières exécutions</p>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', color: '#6b7280', fontSize: '12px' }}>
                <th style={{ padding: '6px 0' }}>Projet</th>
                <th style={{ padding: '6px 0' }}>Type</th>
                <th style={{ padding: '6px 0' }}>Statut</th>
                <th style={{ padding: '6px 0' }}>Date</th>
              </tr>
            </thead>
            <tbody>
              {donnees.dernieresExecutions.map((execution) => (
                <tr key={execution.executionId} style={{ borderTop: '1px solid #e5e7eb', fontSize: '13px' }}>
                  <td style={{ padding: '8px 0' }}>{execution.projetNom}</td>
                  <td style={{ padding: '8px 0' }}>{execution.type === 'TESTS' ? 'Tests' : 'Analyse qualité'}</td>
                  <td style={{ padding: '8px 0' }}>
                    <span style={{
                      backgroundColor: execution.statut === 'TERMINEE' ? '#dcfce7' : '#fee2e2',
                      color: execution.statut === 'TERMINEE' ? '#16a34a' : '#dc2626',
                      padding: '3px 8px',
                      borderRadius: '6px',
                      fontSize: '11px',
                    }}>
                      {execution.statut === 'TERMINEE' ? 'Réussie' : 'Échouée'}
                    </span>
                  </td>
                  <td style={{ padding: '8px 0' }}>{execution.date}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', padding: '12px', flex: 1 }}>
          <p style={{ margin: '0 0 10px 0', fontWeight: 'bold' }}>Mes projets</p>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', color: '#6b7280', fontSize: '12px' }}>
                <th style={{ padding: '6px 0' }}>Nom</th>
                <th style={{ padding: '6px 0' }}>Source</th>
                <th style={{ padding: '6px 0' }}>Dernière exécution</th>
                <th style={{ padding: '6px 0' }}>Statut</th>
              </tr>
            </thead>
            <tbody>
              {donnees.mesProjets.map((projet) => (
                <tr key={projet.projetId} style={{ borderTop: '1px solid #e5e7eb', fontSize: '13px' }}>
                  <td style={{ padding: '8px 0' }}>{projet.nom}</td>
                  <td style={{ padding: '8px 0' }}>{projet.typeSource}</td>
                  <td style={{ padding: '8px 0' }}>{projet.derniereExecution || '—'}</td>
                  <td style={{ padding: '8px 0' }}>{projet.statut}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;