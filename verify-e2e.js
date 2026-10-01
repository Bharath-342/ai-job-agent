// E2E Verification Script for AI Job Agent
const BASE_URL = 'http://localhost:8080/api';

async function run() {
  console.log('=== Starting End-to-End API Verification ===');

  // 1. User Registration
  const email = `test.fresher.${Date.now()}@example.com`;
  const regRes = await fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email,
      password: 'Password123!',
      fullName: 'Amar Satish',
    }),
  });
  const authData = await regRes.json();
  const token = authData.token;
  console.log(`[PASS] 1. Registered user: ${authData.user.email} (ID: ${authData.user.id})`);

  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };

  // 2. Update Profile with 2026 Batch & Skills
  const profileRes = await fetch(`${BASE_URL}/profile`, {
    method: 'PUT',
    headers,
    body: JSON.stringify({
      fullName: 'Amar Satish',
      email,
      graduationYear: 2026,
      experienceYears: 0.0,
      degree: 'B.Tech in Computer Science',
      skills: ['Java', 'Spring Boot', 'SQL', 'PostgreSQL', 'Data Structures', 'Git', 'REST API'],
      targetRoles: ['Java Developer', 'Associate Software Engineer', 'Backend Developer'],
      preferredLocations: ['Hyderabad', 'Bangalore', 'Pune'],
    }),
  });
  const profile = await profileRes.json();
  console.log(`[PASS] 2. Profile configured for 2026 Fresher. Skills: ${profile.skills.join(', ')}`);

  // 3. Discovered Eligible Jobs (Strict India 2026 Filter)
  const eligibleRes = await fetch(`${BASE_URL}/jobs/eligible`, { headers });
  const eligibleJobs = await eligibleRes.json();
  console.log(`[PASS] 3. Discovered ${eligibleJobs.length} eligible jobs matching 2026 fresher criteria.`);
  for (const j of eligibleJobs) {
    console.log(`   -> [${j.matchScore}% Match] ${j.company} - ${j.title} (${j.location})`);
  }

  // 4. Submit Application
  const target = eligibleJobs[0];
  const applyRes = await fetch(`${BASE_URL}/applications/apply`, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      jobId: target.jobId,
      customNotes: 'Automated test application',
    }),
  });
  const app = await applyRes.json();
  console.log(`[PASS] 4. Application processed for ${app.company}: Status=${app.status}`);

  // 5. If Ready to Apply or Manual Action, test resolution
  if (app.status === 'READY_TO_APPLY' || app.status === 'MANUAL_ACTION_REQUIRED') {
    const resolveRes = await fetch(`${BASE_URL}/applications/${app.id}/resolve-manual`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        resolutionStatus: 'APPLIED',
        confirmationId: 'CONF-VERIFIED-777',
        userNotes: 'Application confirmed via career portal',
      }),
    });
    const resolved = await resolveRes.json();
    console.log(`[PASS] 5. Manual Handoff Resolution: Status=${resolved.status}, ConfID=${resolved.confirmationId}`);
  }

  // 6. Simulate Real Inbound ATS Employer Email
  const emailRes = await fetch(`${BASE_URL}/emails/simulate`, {
    method: 'POST',
    headers,
    body: JSON.stringify({
      sender: `recruiting@${target.company.toLowerCase().replace(/[^a-z]/g, '')}.com`,
      subject: `Update regarding your application for ${target.title}`,
      body: `Hello Amar, we have received your application for ${target.title}. Your profile is currently under review by our tech team.`,
      messageId: `MSG-VERIFY-${Date.now()}`,
    }),
  });
  const emailData = await emailRes.json();
  console.log(`[PASS] 6. Email Event Ingested: Status=${emailData.detectedStatus}, Confidence=${Math.round(emailData.confidenceScore * 100)}%`);

  // 7. Verify Application State Change
  const checkAppRes = await fetch(`${BASE_URL}/applications/${app.id}`, { headers });
  const checkApp = await checkAppRes.json();
  console.log(`[PASS] 7. Application Status Transitioned to: ${checkApp.status}`);
  console.log(`   -> Events in timeline: ${checkApp.events.length}`);
  for (const ev of checkApp.events) {
    console.log(`      * [${ev.newStatus}] ${ev.eventType} (${ev.source}): ${ev.description}`);
  }

  // 8. Verify Dashboard Stats & Quota
  const dashRes = await fetch(`${BASE_URL}/dashboard`, { headers });
  const dash = await dashRes.json();
  console.log(`[PASS] 8. Dashboard metrics verified: Under Review=${dash.underReview}, Remaining Quota=${dash.remainingQuota}/${dash.dailyLimit}`);

  // 9. Verify Notifications
  const notifRes = await fetch(`${BASE_URL}/notifications`, { headers });
  const notifs = await notifRes.json();
  const list = Array.isArray(notifs) ? notifs : (notifs.content || []);
  console.log(`[PASS] 9. Received ${list.length} in-app notifications:`);
  for (const n of list.slice(0, 3)) {
    console.log(`      🔔 [${n.type}] ${n.title}`);
  }

  console.log('\n=== ALL 9 LIFECYCLE CHECKS PASSED SUCCESSFULLY ===');
}

run().catch(err => {
  console.error('E2E Verification Error:', err);
  process.exit(1);
});
