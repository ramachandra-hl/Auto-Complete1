(() => {
  let API_BASE_URL = window.location.origin + "/scPro";
  console.log("🌍 API Base URL:", API_BASE_URL);

  const propertyDisplayMap = {
    isAccessoriesRequired: "Accessories",
    isCarcassRequired: "Carcass Material",
    isHandleRequired: "Handles",
    isInternalRequired: "Internals",
    isQPModules: "QP Modules",
    isShutterRequired: "Shutter material",
  };

  const alwaysRequiredFields = ["s.no", "moduleName", "moduleId"];
  const requiredFieldsByProperty = {
    isShutterRequired: ["shutterCodeCode", "shutterCategory", "shutterFinishCode", "shutterColourCode"],
    isAccessoriesRequired: ["accessoryId"],
    isCarcassRequired: ["carcassCode", "carcassColourCode"],
    isHandleRequired: ["handleId"],
    isInternalRequired: ["width", "depth", "height", "internalName", "internalId"],
  };

  // ==========================================================
  // 🔹 Initialize when page is loaded
  // ==========================================================
  document.addEventListener("pageLoaded", async (event) => {
    if (!event.detail.page.includes("scpro")) return;

    console.log("🚀 scPro.js pageLoaded triggered");

    await loadConfig();
    await fetchTestSuites();

    document.getElementById("add-property").onclick = addProperty;
    document.getElementById("runBtn").onclick = runTest;
    document.getElementById("clearBtn").onclick = clearForm;
    document.getElementById("csv-file").addEventListener("change", handleCSV);
  });

  // ==========================================================
  // 🔹 Load API Config
  // ==========================================================
  async function loadConfig() {
    try {
      const res = await fetch(`${API_BASE_URL}/config`);
      if (res.ok) {
        const data = await res.json();
        if (data.apiBaseUrl) {
          API_BASE_URL = data.apiBaseUrl;
          console.log("✅ Loaded API Base URL from backend:", API_BASE_URL);
        }
      } else {
        console.warn("⚠️ Failed to fetch config, using default origin");
      }
    } catch (err) {
      console.warn("⚠️ Config fetch failed:", err);
    }
  }

  // ==========================================================
  // 🔹 Fetch XML Test Suites
  // ==========================================================
  const suiteSelect = document.getElementById("suite");
  const csvInput = document.getElementById("csv-file");
  const csvPreview = document.getElementById("csv-preview");
  const userProperties = document.getElementById("user-properties");
  const responseContainer = document.getElementById("responseContainer");
  let csvData = null;
  let allowFileDialog = false;

  async function fetchTestSuites() {
    try {
      const res = await fetch(`${API_BASE_URL}/test/suites`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const list = await res.json();

      suiteSelect.innerHTML = `<option value="">-- Select Suite --</option>`;
      list.forEach((s) => {
        const o = document.createElement("option");
        o.value = o.textContent = s;
        suiteSelect.appendChild(o);
      });
      console.log("✅ Test suites loaded:", list);
    } catch (err) {
      showMessage(`⚠️ Failed to load suites: ${err.message}`, "error");
    }
  }

  // ==========================================================
  // 🔹 Add Dynamic Property Row
  // ==========================================================
  function addProperty() {
    const selectedKeys = Array.from(document.querySelectorAll(".prop-key")).map(sel => sel.value);
    const availableKeys = Object.keys(propertyDisplayMap).filter(k => !selectedKeys.includes(k));
    if (!availableKeys.length) return showMessage("✅ All properties added!");

    const row = document.createElement("div");
    row.className = "property-row";

    const select = document.createElement("select");
    select.className = "prop-key";
    select.innerHTML = `<option value="">-- Select Property --</option>`;
    availableKeys.forEach(k => {
      const opt = document.createElement("option");
      opt.value = k;
      opt.textContent = propertyDisplayMap[k];
      select.appendChild(opt);
    });

    const remove = document.createElement("button");
    remove.className = "btn-small remove-btn";
    remove.textContent = "🗑️";
    remove.onclick = () => {
      row.remove();
      updateDropdowns();
    };

    row.append(select, remove);
    userProperties.appendChild(row);
    select.addEventListener("change", updateDropdowns);
    updateDropdowns();
  }

  function updateDropdowns() {
    const all = document.querySelectorAll(".prop-key");
    const selected = Array.from(all).map(s => s.value).filter(Boolean);
    all.forEach(sel => {
      const current = sel.value;
      Array.from(sel.options).forEach(opt => {
        opt.disabled = opt.value && opt.value !== current && selected.includes(opt.value);
      });
    });
  }

  function collectProperties() {
    const props = {};
    document.querySelectorAll(".prop-key").forEach(sel => {
      if (sel.value) props[sel.value] = "true";
    });
    return props;
  }

  // ==========================================================
  // 🔹 Handle CSV Upload
  // ==========================================================
  csvInput.addEventListener("click", e => {
    if (!allowFileDialog) {
      e.preventDefault();
      showRequiredFieldsBeforeUpload();
    } else allowFileDialog = false;
  });

  function showRequiredFieldsBeforeUpload() {
    const selected = collectProperties();
    const fields = new Set([...alwaysRequiredFields]);
    Object.keys(selected).forEach(k => {
      if (selected[k] === "true" && requiredFieldsByProperty[k])
        requiredFieldsByProperty[k].forEach(f => fields.add(f));
    });

    const list = document.getElementById("required-fields-list");
    list.innerHTML = "";
    fields.forEach(f => {
      const li = document.createElement("li");
      li.textContent = f;
      list.appendChild(li);
    });

    const modal = document.getElementById("popup-modal");
    modal.style.display = "flex";
    document.getElementById("close-popup").onclick = () => {
      modal.style.display = "none";
      allowFileDialog = true;
      setTimeout(() => csvInput.click(), 150);
    };
  }

  // ==========================================================
  // 🔹 CSV Rendering
  // ==========================================================
  function handleCSV(e) {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = evt => {
      const lines = evt.target.result.trim().split("\n");
      const headers = lines[0].split(",");
      csvData = lines.slice(1).map(line => {
        const obj = {};
        line.split(",").forEach((v, i) => (obj[headers[i]] = v.trim()));
        return obj;
      });
      renderCsvTable(headers, csvData);
    };
    reader.readAsText(file);
  }

  function renderCsvTable(headers, data) {
    const table = document.createElement("table");
    table.className = "csv-table";
    const thead = document.createElement("thead");
    const trh = document.createElement("tr");
    headers.forEach(h => {
      const th = document.createElement("th");
      th.textContent = h;
      trh.appendChild(th);
    });
    thead.appendChild(trh);

    const tbody = document.createElement("tbody");
    data.slice(0, 10).forEach(row => {
      const tr = document.createElement("tr");
      headers.forEach(h => {
        const td = document.createElement("td");
        td.textContent = row[h];
        tr.appendChild(td);
      });
      tbody.appendChild(tr);
    });
    table.append(thead, tbody);
    csvPreview.innerHTML = "";
    csvPreview.appendChild(table);
  }

  // ==========================================================
  // 🔹 Run Test
  // ==========================================================
  async function runTest() {
    const selectedSuite = suiteSelect.value;
    if (!selectedSuite) return showMessage("⚠️ Select a suite!");
    if (!csvData) return showMessage("⚠️ Upload a CSV first!");

    const props = collectProperties();
    const projectId = document.getElementById("projectId").value.trim();
    const env = document.getElementById("env").value.trim();
    const custType = document.getElementById("customerType").value.trim();

    if (projectId) props.projectID = projectId;
    if (env) props.environment = env;
    if (custType) props.customerType=custType

    const qs = new URLSearchParams(props).toString();
    const url = `${API_BASE_URL}/run?xmlFile=${selectedSuite}${qs ? "&" + qs : ""}`;

    const btn = document.getElementById("runBtn");
    btn.textContent = "Running...";
    btn.classList.add("running");

    try {
      const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(csvData),
      });

      const data = await res.json();
      btn.textContent = "Run";
      btn.classList.remove("running");
      responseContainer.innerHTML = "";

      if (data.status === "success") {
        showMessage("✅ Test completed successfully!", "success");

        if (data.htmlReportUrl) {
          testReporter(`<a href="${data.htmlReportUrl}" target="_blank" class="btn-report">📄 View HTML Report</a>`);
        }

        if (Array.isArray(data.csvReports) && data.csvReports.length > 0) {
          data.csvReports.forEach((report) => {
            testReporter(`<a href="${report.url}" class="btn-download" download>📥 Download ${report.name}</a>`);
          });
        }
      } else {
        showMessage(`❌ Test failed: ${data.message}`, "error");
      }
    } catch (err) {
      showMessage("❌ Error: " + err.message, "error");
    }
  }

  // ==========================================================
  // 🔹 Clear Form
  // ==========================================================
  function clearForm() {
    suiteSelect.value = "";
    csvInput.value = "";
    csvPreview.innerHTML = "<p>No CSV selected</p>";
    userProperties.innerHTML = "";
    csvData = null;
    showMessage("🧹 Cleared form.", "info");
  }

  // ==========================================================
  // 🔹 Display Messages
  // ==========================================================
  function showMessage(msg, type = "info") {
    responseContainer.innerHTML += `<p class="${type}">${msg}</p>`;
  }

  function testReporter(htmlContent) {
    const div = document.createElement("div");
    div.className = "report-link";
    div.innerHTML = htmlContent;
    responseContainer.appendChild(div);
  }

  // ==========================================================
  // 🩵 Fallback: Direct Run (if event missed)
  // ==========================================================
  if (document.getElementById("suite")) {
    console.log("⚙️ Running scPro.js directly (fallback init)");
    loadConfig().then(fetchTestSuites);
    document.getElementById("add-property").onclick = addProperty;
    document.getElementById("runBtn").onclick = runTest;
    document.getElementById("clearBtn").onclick = clearForm;
    document.getElementById("csv-file").addEventListener("change", handleCSV);
  }
})();
