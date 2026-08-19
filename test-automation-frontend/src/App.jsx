import { Routes, Route } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './pages/Dashboard';
import Projets from './pages/Projets';
import ConfigurationTest from './pages/ConfigurationTest';
function App() {
  return (
    <div style={{ display: 'flex' }}>
      <Sidebar />
      <div style={{ flex: 1 }}>
        <Header />
        <div style={{ padding: '0 20px', color: '#111827' }}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/projets" element={<Projets />} />
            <Route path="/configurations" element={<ConfigurationTest />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}

export default App;