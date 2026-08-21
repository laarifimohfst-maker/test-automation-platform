import { Routes, Route } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './pages/Dashboard';
import Projets from './pages/Projets';
import ConfigurationTest from './pages/ConfigurationTest';
import Executions from './pages/Executions';
import ResultatsTests from './pages/ResultatsTests';
import AnalyseQualite from './pages/AnalyseQualite';
import Profil from './pages/Profil';
import { AlertDialogProvider } from './components/AlertDialog';

function App() {
  return (
    <AlertDialogProvider>
      <div style={{ display: 'flex' }}>
        <Sidebar />
        <div style={{ flex: 1, minWidth: 0 }}>
          <Header />
          <div style={{ padding: '0 20px', color: '#111827' }}>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/projets" element={<Projets />} />
              <Route path="/configurations" element={<ConfigurationTest />} />
              <Route path="/executions" element={<Executions />} />
              <Route path="/resultats" element={<ResultatsTests />} />
              <Route path="/qualite" element={<AnalyseQualite />} />
              <Route path="/profil" element={<Profil />} />
            </Routes>
          </div>
        </div>
      </div>
    </AlertDialogProvider>
  );
}

export default App;
