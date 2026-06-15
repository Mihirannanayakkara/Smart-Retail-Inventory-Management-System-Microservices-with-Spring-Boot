import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import UsersPage from './pages/UsersPage';
import Suppliers from './pages/Suppliers';
import Restocks from './pages/Restocks';
import Notifications from './pages/Notifications';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/products" element={<Products />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/suppliers" element={<Suppliers />} />
          <Route path="/restocks" element={<Restocks />} />
          <Route path="/notifications" element={<Notifications />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
