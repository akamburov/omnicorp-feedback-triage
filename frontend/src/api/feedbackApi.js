const API_BASE = '/api/v1/feedback';

export async function submitFeedback(data) {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  if (!response.ok) {
    throw new Error('Failed to submit feedback');
  }
  return response.json();
}

export async function getAllFeedback() {
  const response = await fetch(API_BASE);
  if (!response.ok) {
    throw new Error('Failed to fetch feedback history');
  }
  return response.json();
}
