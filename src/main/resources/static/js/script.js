const links = document.querySelectorAll("nav a");
const contentDiv = document.getElementById("content");
let currentScript = null;

// ==========================================================
// 🔹 Load Page + Its JS Dynamically
// ==========================================================
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

    // Wait a bit for DOM to settle before adding new JS
    await new Promise((r) => setTimeout(r, 30));

    // Add the page's JS dynamically
    const script = document.createElement("script");
    script.src = `/js/${scriptFile}?v=${Date.now()}`;
    script.type = "text/javascript";

    script.onload = () => {
      console.log(`✅ ${scriptFile} executed`);
      // ⏱️ Delay event dispatch slightly to ensure the script registered its listeners
      setTimeout(() => {
        console.log(`📢 Dispatching pageLoaded event for ${page}`);
        document.dispatchEvent(
          new CustomEvent("pageLoaded", { detail: { page } })
        );
      }, 100);
    };

    script.onerror = () => console.error(`❌ Failed to load ${scriptFile}`);

    document.body.appendChild(script);
    currentScript = script;
  } catch (err) {
    console.error("❌ loadPage error:", err);
    contentDiv.innerHTML = `<p style="color:red;">Failed to load ${page}</p>`;
  }
}

// ==========================================================
// 🔹 Handle Navigation
// ==========================================================
links.forEach((link) => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    links.forEach((l) => l.classList.remove("active"));
    link.classList.add("active");

    const page = link.dataset.page;
    const scriptFile = link.dataset.script;
    loadPage(page, scriptFile);
    window.history.pushState({ page, scriptFile }, "", `#${page}`);
  });
});

// ==========================================================
// 🔹 Handle Browser Back / Forward
// ==========================================================
window.addEventListener("popstate", (e) => {
  const page = e.state?.page || "roster.html";
  const scriptFile = e.state?.scriptFile || "roster.js";
  loadPage(page, scriptFile);
  setActive(page);
});

// ==========================================================
// 🔹 Highlight Active Nav Link
// ==========================================================
function setActive(page) {
  links.forEach((a) => {
    a.classList.toggle("active", a.dataset.page === page);
  });
}

// ==========================================================
// 🔹 Initial Load
// ==========================================================
(function init() {
  const hash = window.location.hash.replace("#", "");
  const page = hash || "roster.html";
  const scriptFile = page.replace(".html", ".js");
  loadPage(page, scriptFile);
  setActive(page);
})();
