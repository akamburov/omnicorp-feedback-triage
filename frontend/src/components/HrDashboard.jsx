import React, { useState, useEffect, useCallback } from 'react';
import { getAllFeedback } from '../api/feedbackApi';
import { RefreshCw, Filter, CheckCircle2, Clock, AlertTriangle } from 'lucide-react';

export default function HrDashboard() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [filterCategory, setFilterCategory] = useState('ALL');

  const fetchItems = useCallback(async (isInitial = false) => {
    if (isInitial) {
      setLoading(true);
    } else {
      setIsRefreshing(true);
    }

    try {
      const data = await getAllFeedback();
      setItems(data);
    } catch (err) {
      console.error('Failed to fetch feedback history:', err);
    } finally {
      if (isInitial) setLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    let isMounted = true;

    const loadData = async () => {
      setLoading(true);
      try {
        const data = await getAllFeedback();
        if (isMounted) setItems(data);
      } catch (err) {
        console.error('Failed to fetch feedback history:', err);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadData();

    const interval = setInterval(async () => {
      try {
        const data = await getAllFeedback();
        if (isMounted) setItems(data);
      } catch (err) {
        console.error('Background poll failed:', err);
      }
    }, 4000);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  const filteredItems = items.filter(item => {
    if (filterCategory !== 'ALL' && item.category !== filterCategory) return false;
    return true;
  });

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'CRITICAL':
      case 'HIGH':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-rose-100 text-rose-800 border border-rose-200">HIGH</span>;
      case 'MEDIUM':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-100 text-amber-800 border border-amber-200">MEDIUM</span>;
      case 'LOW':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-emerald-100 text-emerald-800 border border-emerald-200">LOW</span>;
      default:
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-700">UNKNOWN</span>;
    }
  };

  const getCategoryBadge = (category) => {
    return (
      <span className="px-2.5 py-1 text-xs font-medium rounded-md bg-blue-50 text-blue-700 border border-blue-100">
        {category || 'GENERAL'}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header and Controls */}
      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 flex items-center space-x-2">
            <span>HR Automated Triage Dashboard</span>
          </h2>
          <p className="text-gray-500 text-sm mt-1">Real-time automated categorization & AI recommendations</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-gray-500" />
            <select
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              className="text-sm border border-gray-300 rounded-lg px-3 py-1.5 focus:ring-2 focus:ring-blue-500 outline-none"
            >
              <option value="ALL">All Categories</option>
              <option value="FACILITIES">Facilities</option>
              <option value="IT">IT</option>
              <option value="HR">HR</option>
              <option value="MANAGEMENT">Management</option>
              <option value="OPERATIONAL">Operational</option>
            </select>
          </div>

          <button
            onClick={() => fetchItems(false)}
            className="p-2 border border-gray-300 hover:bg-gray-50 rounded-lg transition text-gray-700 flex items-center space-x-1 text-sm font-medium"
          >
            <RefreshCw className={`w-4 h-4 ${(loading || isRefreshing) ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Submissions List */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-center py-12 bg-white rounded-xl border border-gray-100">
            <p className="text-gray-500 font-medium">Loading feedback submissions...</p>
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl border border-gray-100">
            <p className="text-gray-500 font-medium">No feedback submissions found matching your filter.</p>
          </div>
        ) : (
          filteredItems.map((item) => (
            <div key={item.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:border-gray-200 transition">
              <div className="p-6">
                <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                  <div className="flex items-center space-x-3">
                    {getCategoryBadge(item.category)}
                    {getPriorityBadge(item.priority)}
                    <span className="text-xs text-gray-500">
                      Dept: <strong className="text-gray-700">{item.department}</strong>
                    </span>
                    <span className="text-xs text-gray-500">
                      From: <strong className="text-gray-700">{item.employeeName}</strong>
                    </span>
                  </div>

                  <div className="flex items-center space-x-2">
                    {item.status === 'TRIAGED' && (
                      <span className="flex items-center text-xs text-emerald-600 font-medium bg-emerald-50 px-2.5 py-1 rounded-full">
                        <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                        Triaged by {item.processedBy}
                      </span>
                    )}
                    {item.status === 'PENDING' && (
                      <span className="flex items-center text-xs text-amber-600 font-medium bg-amber-50 px-2.5 py-1 rounded-full animate-pulse">
                        <Clock className="w-3.5 h-3.5 mr-1" />
                        Processing Triage...
                      </span>
                    )}
                    {item.status === 'FAILED' && (
                      <span className="flex items-center text-xs text-rose-600 font-medium bg-rose-50 px-2.5 py-1 rounded-full">
                        <AlertTriangle className="w-3.5 h-3.5 mr-1" />
                        Triage Failed
                      </span>
                    )}
                  </div>
                </div>

                {/* Original Feedback Content */}
                <div className="mb-4">
                  <p className="text-gray-800 text-sm bg-gray-50 p-3 rounded-lg border border-gray-100 italic">
                    "{item.content}"
                  </p>
                </div>

                {/* AI Summary and Action Plan */}
                {item.status === 'TRIAGED' && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-3 border-t border-gray-100">
                    <div>
                      <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider block mb-1">
                        AI Summary
                      </span>
                      <p className="text-sm font-medium text-gray-900">{item.summary}</p>
                    </div>

                    <div>
                      <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider block mb-1">
                        Recommended Actionable Steps
                      </span>
                      <p className="text-sm text-blue-900 bg-blue-50/60 p-2.5 rounded-lg border border-blue-100/50">
                        {item.actionableSteps}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
