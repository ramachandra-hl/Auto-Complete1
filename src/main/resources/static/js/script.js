const links = document.querySelectorAll("nav a");
const contentDiv = document.getElementById("content");
let currentScript = null;

// Load page + JS dynamically
async function loadPage(page, scriptFile) {
  try {
    console.log(`🔄 Loading page: ${page}`);

    // Load HTML (force no-cache)
    const res = await fetch(`/pages/${page}?v=${Date.now()}`);
    if (!res.ok) throw new Error(`Page not found: ${page}`);

    const html = await res.text();
    contentDiv.innerHTML = html;

    // Remove previous JS
    if (currentScript) {
      console.log("🧹 Removing previous script:", currentScript.src);
      currentScript.remove();
      currentScript = null;
    }

    // Wait for DOM to update
    await new Promise(r => setTimeout(r, 30));

    // Dynamically add the JS for this page
    const script = document.createElement("script");
    script.src = `/js/${scriptFile}?v=${Date.now()}`;
    script.type = "text/javascript";

    // ✅ Trigger a custom "pageLoaded" event after HTML + JS is ready
    script.onload = () => {
      console.log(`✅ ${scriptFile} executed`);
      document.dispatchEvent(
        new CustomEvent("pageLoaded", { detail: { page } })
      );
    };

    script.onerror = () => console.error(`❌ Failed to load ${scriptFile}`);

    document.body.appendChild(script);
    currentScript = script;
  } catch (err) {
    console.error("❌ loadPage error:", err);
    contentDiv.innerHTML = `<p style="color:red;">Failed to load ${page}</p>`;
  }
}

// Handle navigation clicks
links.forEach(link => {
  link.addEventListener("click", e => {
    e.preventDefault();
    links.forEach(l => l.classList.remove("active"));
    link.classList.add("active");

    const page = link.dataset.page;
    const scriptFile = link.dataset.script;
    loadPage(page, scriptFile);
    window.history.pushState({ page, scriptFile }, "", `#${page}`);
  });
});

// Handle back/forward
window.addEventListener("popstate", e => {
  const page = e.state?.page || "roster.html";
  const scriptFile = e.state?.scriptFile || "roster.js";
  loadPage(page, scriptFile);
  setActive(page);
});

// Highlight correct nav link
function setActive(page) {
  links.forEach(a => {
    a.classList.toggle("active", a.dataset.page === page);
  });
}

// Initial page load
(function init() {
  const hash = window.location.hash.replace("#", "");
  const page = hash || "roster.html";
  const scriptFile = page.replace(".html", ".js");
  loadPage(page, scriptFile);
  setActive(page);
})();
