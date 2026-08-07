import React, { useState } from 'react';
import EmployeePortal from './components/EmployeePortal';
import HrDashboard from './components/HrDashboard';
import { Building2, UserCircle, ShieldCheck } from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('employee');

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Top Navbar */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-blue-600 text-white rounded-lg shadow-sm">
              <Building2 className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-lg font-bold text-gray-900 leading-tight">OmniCorp Solutions</h1>
              <p className="text-xs text-gray-500 font-medium">Automated Feedback Triage System</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="flex space-x-2 bg-slate-100 p-1 rounded-xl">
            <button
              onClick={() => setActiveTab('employee')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-semibold transition ${
                activeTab === 'employee'
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <UserCircle className="w-4 h-4" />
              <span>Employee Portal</span>
            </button>

            <button
              onClick={() => setActiveTab('hr')}
              className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-sm font-semibold transition ${
                activeTab === 'hr'
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <ShieldCheck className="w-4 h-4" />
              <span>HR Dashboard</span>
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className={activeTab === 'employee' ? 'block' : 'hidden'}>
          <EmployeePortal onSubmissionSuccess={() => {}} />
        </div>
        <div className={activeTab === 'hr' ? 'block' : 'hidden'}>
          <HrDashboard />
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-4 text-center text-xs text-gray-500">
        OmniCorp Solutions Feedback Triage System &copy; 2025 | Powered by Java 17 Spring Boot & AI Strategy
      </footer>
    </div>
  );
}
