import React, { useState } from 'react';
import { submitFeedback } from '../api/feedbackApi';
import { Send, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';

export default function EmployeePortal({ onSubmissionSuccess }) {
  const [content, setContent] = useState('');
  const [department, setDepartment] = useState('Engineering');
  const [employeeName, setEmployeeName] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await submitFeedback({ content, department, employeeName });
      setSuccess(true);
      setContent('');
      if (onSubmissionSuccess) onSubmissionSuccess();
    } catch (err) {
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto bg-white p-8 rounded-xl shadow-sm border border-gray-100">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Employee Feedback Portal</h2>
        <p className="text-gray-600 mt-1">
          Share your workplace concerns, ideas, or issues. Our AI system automatically routes feedback to the relevant team.
        </p>
      </div>

      {success && (
        <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-lg flex items-center space-x-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          <div>
            <p className="font-semibold">Feedback Submitted Successfully!</p>
            <p className="text-sm text-emerald-700">Your input has been received and sent for automated AI triage.</p>
          </div>
        </div>
      )}

      {error && (
        <div className="mb-6 p-4 bg-rose-50 border border-rose-200 text-rose-800 rounded-lg flex items-center space-x-3">
          <AlertCircle className="w-5 h-5 text-rose-600 flex-shrink-0" />
          <p className="text-sm">{error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Department
          </label>
          <select
            value={department}
            onChange={(e) => setDepartment(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
          >
            <option value="Engineering">Engineering</option>
            <option value="Facilities">Facilities</option>
            <option value="Human Resources">Human Resources</option>
            <option value="Product">Product</option>
            <option value="Sales">Sales</option>
            <option value="Operations">Operations</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Your Name (Optional)
          </label>
          <input
            type="text"
            placeholder="John Doe (or leave blank for Anonymous)"
            value={employeeName}
            onChange={(e) => setEmployeeName(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Feedback & Details <span className="text-rose-500">*</span>
          </label>
          <textarea
            required
            rows={5}
            placeholder="Describe your concern or recommendation in detail (e.g. Facilities issues, IT equipment requests, HR questions)..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
          />
        </div>

        <button
          type="submit"
          disabled={loading || !content.trim()}
          className="w-full py-3 px-6 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg shadow hover:shadow-md transition flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              <span>Submitting...</span>
            </>
          ) : (
            <>
              <Send className="w-5 h-5" />
              <span>Submit Feedback</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
}
