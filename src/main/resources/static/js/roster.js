(() => {
const customerType = document.getElementById('customerType');
const city = document.getElementById('city');
const customerName = document.getElementById('customerName');
const env = document.getElementById('env');
const mobilePrefix = document.getElementById('mobilePrefix');
const inputs = document.querySelectorAll('#customerName, #lead, #mobilePrefix, #env, #customerType, #city');
const runBtn = document.getElementById('runBtn');
const noOfLeads = document.getElementById('lead');
const showroomContainer = document.getElementById('showroomContainer');
const showroom = document.getElementById('showroom');
const responseContainer = document.getElementById('responseContainer');

let apiBaseUrl = window.location.origin;


// --- City data mapping ---
const cityData = {
  "DC": { "Bengaluru": "1" },
  "HL": {
    "Bengaluru": "1", "Hyderabad": "8", "Chennai": "2", "Mumbai": "3", "Thane": "11",
    "Pune": "10", "Mysore": "14", "Coimbatore": "27", "Kolkata": "4", "Ahmedabad": "38",
    "Visakhapatnam": "6", "Surat": "31", "Gurgaon": "9", "New Delhi": "7", "Noida": "26",
    "Bhubaneswar": "28", "Jaipur": "37", "Lucknow": "12", "Kochi": "5", "Nagpur": "30",
    "Madurai": "35", "Mangalore": "13", "Nashik": "34", "Patna": "15", "Ranchi": "32",
    "Salem": "29", "Vijayawada": "18", "Trichy": "36"
  },
  "HFN": {
    "Bengaluru": "1", "Hyderabad": "8", "Kolkata": "4", "Jamshedpur": "25",
    "Coimbatore": "27", "Vijayawada": "18", "Tirupati": "16", "Mysore": "14",
    "Chennai": "2", "Guwahati": "17", "Patna": "15", "Trivandrum": "22", "Siliguri": "21",
    "Warangal": "23", "Visakhapatnam": "6", "Nizamabad": "19", "Karimnagar": "24",
    "Mangalore": "13", "Shimgoa": "20", "Kochi": "5", "Ranchi": "3", "Ahmedabad": "38",
    "Thane": "11", "Nagpur": "30"
  }
};

// --- Prefix Validation ---
mobilePrefix.addEventListener("input", () => {
  const value = parseInt(mobilePrefix.value, 10);
  if (mobilePrefix.value.length > 2) mobilePrefix.value = mobilePrefix.value.slice(0, 2);
  if (isNaN(value) || value < 60 || value > 99) {
    mobilePrefix.classList.add("invalid");
    mobilePrefix.setCustomValidity("Prefix must be between 60 and 99");
  } else {
    mobilePrefix.classList.remove("invalid");
    mobilePrefix.setCustomValidity("");
  }
});

// --- Populate City Dropdown ---
customerType.addEventListener('change', () => {
  const selectedType = customerType.value;
  const cities = cityData[selectedType] || {};
  city.innerHTML = '<option value="">Select City</option>';
  Object.entries(cities).forEach(([name, id]) => {
    const opt = document.createElement('option');
    opt.value = id;
    opt.text = name;
    city.add(opt);
  });
  updateShowRoom();
});

// --- Update Showroom List ---
async function updateShowRoom() {
  const selectedType = customerType.value;
  if (selectedType !== 'HL' && selectedType !== 'HFN') {
    showroomContainer.style.display = 'none';
    showroom.innerHTML = '';
    return;
  }
  showroomContainer.style.display = 'block';

  const payload = {
    customerType: selectedType,
    environment: env.value.trim(),

  };

  try {
    // Fetch base API URL
    const configResp = await fetch('/roster/config');
    if (configResp.ok) {
      const configData = await configResp.json();
      if (configData.apiBaseUrl) apiBaseUrl = configData.apiBaseUrl;
    }

    const resp = await fetch(`${apiBaseUrl}/roster/helper/getShowroomsList`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    const data = await resp.json();

    showroom.innerHTML = `<option value="">Select Showroom</option>`;
    if (data.status === 'success' && Array.isArray(data.showrooms)) {
      data.showrooms.forEach(s => {
        const name = s.showroom_name || s.name || 'Unnamed';
        const sfId = s.live_sf_id || 'N/A';
        const opt = document.createElement('option');
        opt.value = sfId;
        opt.textContent = name;
        showroom.appendChild(opt);
      });
    }
  } catch (err) {
    console.error('❌ Failed to load showrooms:', err);
    showroom.innerHTML = `<option value="">Error loading showrooms</option>`;
  }
}

// --- Input Validation ---
inputs.forEach(input => {
  input.addEventListener('input', checkInputs);
  input.addEventListener('change', checkInputs);
});
function checkInputs() {
  const allFilled = Array.from(inputs).every(i => i.value.trim() !== '');
  runBtn.disabled = !allFilled;
  runBtn.classList.toggle('active', allFilled);
}

// --- Lead Creation ---
async function createLead() {
  const payload = {
    customerName: customerName.value.trim(),
    environment: env.value.trim(),
    customerType: customerType.value.trim(),
    userSelectedCityProperty: city.value.trim(),
    mobileNoStarting2digitPrefix: mobilePrefix.value.trim(),
    noOfLeads: noOfLeads.value.trim(),
    showroomId: showroom.value.trim(),
  };

  responseContainer.innerHTML = '';
  runBtn.textContent = 'Running...';
  runBtn.classList.replace('active', 'running');
  runBtn.disabled = true;

  try {

    const response = await fetch(`${apiBaseUrl}/roster/LeadCreation`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const data = await response.json();
    console.log('✅ Lead creation response:', data);

    const output = data.output || {};
    const customerIds = Array.isArray(output.customerId)
      ? output.customerId
      : output.customerId ? [output.customerId] : [];
      const projectIds = Array.isArray(output.projectId)
      ? output.projectId
      : output.projectId ? [output.projectId] : [];
    const projectUrls = Array.isArray(output.projectFullURL)
      ? output.projectFullURL
      : output.projectFullURL ? [output.projectFullURL] : [];

    responseContainer.innerHTML = `
      <div><strong>Status:</strong> ${data.status}</div>
      <hr>
      <div><strong>Customer IDs:</strong></div>
      ${customerIds.map(id => `<div class="response-item">• ${id}</div>`).join('') || '<div>N/A</div>'}
      <hr>
       <div><strong>Project IDs:</strong></div>
       ${projectIds.map(id => `<div class="response-item">• ${id}</div>`).join('') || '<div>N/A</div>'}
       <hr>
      <div><strong>Project URLs:</strong></div>
      ${projectUrls.map(url => `<div class="response-item">• <a href="${url}" target="_blank">${url}</a></div>`).join('') || '<div>N/A</div>'}
    `;


    runBtn.textContent = 'Success';
    runBtn.classList.replace('running', 'success');
  } catch (err) {
    console.error('❌ Lead creation failed:', err);
    responseContainer.innerHTML = `<div style="color:red;"><strong>Error:</strong> ${err.message}</div>`;
    runBtn.textContent = 'Failed';
    runBtn.classList.replace('running', 'error');
  } finally {
    setTimeout(() => {
      runBtn.textContent = 'Create Lead';
      runBtn.classList.remove('success', 'error', 'running');
      runBtn.classList.add('active');
      runBtn.disabled = false;
    }, 3000);
  }
}

runBtn.addEventListener('click', createLead);
})();