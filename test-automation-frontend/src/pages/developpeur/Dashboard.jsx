import { useState, useEffect } from 'react';
import { obtenirDashboard } from '../../services/developpeur/dashboardService';
import StatCard from '../../components/StatCard';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { Folder, Play, CheckCircle, XCircle, AlertCircle, Shield } from 'lucide-react';
import './Dashboard.css';
import { obtenirUtilisateurId } from '../../services/auth/authStorage';

function Dashboard() {
  const [donnees, setDonnees] = useState(null);

  useEffect(() => {
    obtenirDashboard(obtenirUtilisateurId()).then((resultat) => {
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
    <div className="container">
      <div className="statsRow">
        <StatCard titre="Projets" valeur={donnees.nbProjets} couleur="#0284c7" fondIcone="#e0f2fe" icone={<Folder size={20} />} />
        <StatCard titre="Exécutions" valeur={donnees.nbExecutions} couleur="#16a34a" fondIcone="#dcfce7" icone={<Play size={20} />} />
        <StatCard titre="Tests réussis" valeur={donnees.testsReussis} couleur="#16a34a" fondIcone="#dcfce7" icone={<CheckCircle size={20} />} />
        <StatCard titre="Tests échoués" valeur={donnees.testsEchoues} couleur="#dc2626" fondIcone="#fee2e2" icone={<XCircle size={20} />} />
        <StatCard titre="Quality Gates OK" valeur={donnees.gatesOk} couleur="#d97706" fondIcone="#fef3c7" icone={<AlertCircle size={20} />} />
        <StatCard titre="Quality Gates KO" valeur={donnees.gatesKo} couleur="#0284c7" fondIcone="#e0f2fe" icone={<Shield size={20} />} />
      </div>

      <div className="chartsRow">
        <div className="chartCardWide">
          <p className="chartTitle">Évolution des exécutions</p>
          <ResponsiveContainer width="100%" height={180}>
            <LineChart data={donnees.evolutionExecutions}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Line type="monotone" dataKey="total" stroke="#0284c7" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {graphiquesDonut.map((graphique) => {
          const total = graphique.data.reduce((somme, entree) => somme + entree.valeur, 0);

          return (
            <div key={graphique.titre} className="chartCard">
              <p className="chartTitle">{graphique.titre}</p>
              <div className="donutCenter">
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
                    <div key={entree.nom} className="legendRow">
                      <span className="legendDot" style={{ backgroundColor: entree.couleur }}></span>
                      <span className="legendText">
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

      <div className="tablesRow">
        <div className="tableCard">
          <p className="tableTitle">Dernières exécutions</p>
          <table className="table">
            <thead>
              <tr className="theadRow">
                <th className="th">Projet</th>
                <th className="th">Type</th>
                <th className="th">Statut</th>
                <th className="th">Date</th>
              </tr>
            </thead>
            <tbody>
              {donnees.dernieresExecutions.map((execution) => (
                <tr key={execution.executionId} className="row">
                  <td className="td">{execution.projetNom}</td>
                  <td className="td">{execution.type === 'TESTS' ? 'Tests' : 'Analyse qualité'}</td>
                  <td className="td">
                    <span className={`badge ${execution.statut === 'TERMINEE' ? 'badgeSucces' : 'badgeEchec'}`}>
                      {execution.statut === 'TERMINEE' ? 'Réussie' : 'Échouée'}
                    </span>
                  </td>
                  <td className="td">{execution.date}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="tableCard">
          <p className="tableTitle">Mes projets</p>
          <table className="table">
            <thead>
              <tr className="theadRow">
                <th className="th">Nom</th>
                <th className="th">Source</th>
                <th className="th">Dernière exécution</th>
                <th className="th">Statut</th>
              </tr>
            </thead>
            <tbody>
              {donnees.mesProjets.map((projet) => (
                <tr key={projet.projetId} className="row">
                  <td className="td">{projet.nom}</td>
                  <td className="td">{projet.typeSource}</td>
                  <td className="td">{projet.derniereExecution || '—'}</td>
                  <td className="td">{projet.statut}</td>
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
