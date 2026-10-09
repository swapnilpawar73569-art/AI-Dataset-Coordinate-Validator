/**
 * GeoValidate AI — Frontend Controller & Visualization Engine
 */

(function () {
  'use strict';

  // Application State
  const state = {
    format: 'csv',
    rawContent: '',
    results: [],
    summary: {
      totalCount: 0,
      validCount: 0,
      invalidCount: 0,
      passRate: 0,
      duplicateCount: 0,
      outlierCount: 0,
      bboxBreachCount: 0,
    },
    activeFilter: 'all',
    searchQuery: '',
    map: null,
    markers: [],
    bboxLayer: null,
    backendOnline: false,
    rules: {
      checkFormat: true,
      checkRange: true,
      checkDuplicates: true,
      checkOutliers: true,
      checkPrecision: true,
      checkBoundingBox: true,
      outlierIqr: 1.5,
      precisionPlaces: 6,
      bboxRegion: 'India Region',
      bboxMinLat: 6.0,
      bboxMaxLat: 37.5,
      bboxMinLon: 68.0,
      bboxMaxLon: 97.5,
    }
  };

  // Preset Regions for Bounding Box
  const PRESETS = {
    india: { name: 'India Region', minLat: 6.0, maxLat: 37.5, minLon: 68.0, maxLon: 97.5 },
    usa: { name: 'USA Contiguous', minLat: 24.5, maxLat: 49.38, minLon: -125.0, maxLon: -66.9 },
    europe: { name: 'Europe Region', minLat: 35.0, maxLat: 71.0, minLon: -10.0, maxLon: 40.0 },
    global: { name: 'Global Earth', minLat: -90.0, maxLat: 90.0, minLon: -180.0, maxLon: 180.0 },
  };

  // Sample Datasets
  const SAMPLE_CSV = `label,latitude,longitude
New_Delhi,28.613900,77.209000
Mumbai,19.076000,72.877700
Bengaluru,12.971600,77.594600
Hyderabad,17.385000,78.486700
Chennai,13.082700,80.270700
Kolkata,22.572600,88.363900
Ahmedabad,23.022500,72.571400
Jaipur,26.912400,75.787300
Delhi_Duplicate,28.613900,77.209000
Error_LatOver90,98.543200,75.200000
Error_LonOver180,24.120000,195.430000
Error_MissingLat,,77.102500
Error_CorruptFormat,21.170240,NOT_A_NUMBER
Outlier_Antarctica,-82.862800,135.000000
Outlier_Greenland,72.000000,-40.000000`;

  const SAMPLE_JSON = JSON.stringify([
    { label: "New_Delhi", latitude: 28.6139, longitude: 77.2090 },
    { label: "Mumbai", latitude: 19.0760, longitude: 72.8777 },
    { label: "Bengaluru", latitude: 12.9716, longitude: 77.5946 },
    { label: "Hyderabad", latitude: 17.3850, longitude: 78.4867 },
    { label: "Chennai", latitude: 13.0827, longitude: 80.2707 },
    { label: "Delhi_Duplicate", latitude: 28.6139, longitude: 77.2090 },
    { label: "Error_LatOver90", latitude: 98.5432, longitude: 75.2000 },
    { label: "Error_MissingLat", latitude: null, longitude: 77.1025 },
    { label: "Error_CorruptFormat", latitude: 21.17024, longitude: "BAD_VALUE" },
    { label: "Outlier_Antarctica", latitude: -82.8628, longitude: 135.0000 }
  ], null, 2);

  // DOM Elements
  const elements = {
    backendDot: document.getElementById('backend-status-dot'),
    backendText: document.getElementById('backend-status-text'),
    btnQuickSample: document.getElementById('btn-quick-sample'),
    btnOpenSettings: document.getElementById('btn-open-settings'),
    btnCloseSettings: document.getElementById('btn-close-settings'),
    settingsModal: document.getElementById('settings-modal'),
    btnExportDropdown: document.getElementById('btn-export-dropdown'),
    exportMenu: document.getElementById('export-menu'),
    tabCsv: document.getElementById('tab-format-csv'),
    tabJson: document.getElementById('tab-format-json'),
    fileDropzone: document.getElementById('file-dropzone'),
    fileInput: document.getElementById('file-input'),
    btnBrowseFile: document.getElementById('btn-browse-file'),
    chipCsv: document.getElementById('chip-sample-csv'),
    chipJson: document.getElementById('chip-sample-json'),
    rawDataInput: document.getElementById('raw-data-input'),
    rawLinesBadge: document.getElementById('raw-lines-badge'),
    btnValidate: document.getElementById('btn-validate'),
    kpiTotal: document.getElementById('kpi-total'),
    kpiValid: document.getElementById('kpi-valid'),
    kpiInvalid: document.getElementById('kpi-invalid'),
    kpiOutliers: document.getElementById('kpi-outliers'),
    kpiDuplicates: document.getElementById('kpi-duplicates'),
    kpiRateText: document.getElementById('kpi-rate-text'),
    mapPointCounter: document.getElementById('map-point-counter'),
    btnFitBounds: document.getElementById('btn-fit-bounds'),
    btnToggleBBox: document.getElementById('btn-toggle-bbox-overlay'),
    tableSearchInput: document.getElementById('table-search-input'),
    resultsTbody: document.getElementById('results-tbody'),
    filterTabs: document.querySelectorAll('.filter-tab'),
    toastContainer: document.getElementById('toast-container'),
    // Settings inputs
    ruleFormat: document.getElementById('rule-format'),
    ruleRange: document.getElementById('rule-range'),
    ruleDuplicates: document.getElementById('rule-duplicates'),
    ruleOutliers: document.getElementById('rule-outliers'),
    rulePrecision: document.getElementById('rule-precision'),
    ruleBBox: document.getElementById('rule-bbox'),
    inputIqrMultiplier: document.getElementById('input-iqr-multiplier'),
    valIqrMultiplier: document.getElementById('val-iqr-multiplier'),
    inputPrecisionPlaces: document.getElementById('input-precision-places'),
    valPrecisionPlaces: document.getElementById('val-precision-places'),
    bboxRegion: document.getElementById('bbox-region'),
    bboxMinLat: document.getElementById('bbox-min-lat'),
    bboxMaxLat: document.getElementById('bbox-max-lat'),
    bboxMinLon: document.getElementById('bbox-min-lon'),
    bboxMaxLon: document.getElementById('bbox-max-lon'),
    btnSaveRules: document.getElementById('btn-save-rules'),
    btnResetRules: document.getElementById('btn-reset-rules'),
    presetButtons: document.querySelectorAll('.preset-btn'),
  };

  // ==================== INITIALIZATION ====================
  function init() {
    initMap();
    initEventListeners();
    checkBackendHealth();

    // Load initial sample CSV dataset
    loadDataContent(SAMPLE_CSV, 'csv');
    executeValidation();
  }

  // ==================== MAP INITIALIZATION ====================
  function initMap() {
    if (typeof L === 'undefined') {
      console.warn('Leaflet not loaded yet.');
      return;
    }

    state.map = L.map('coordinate-map', {
      center: [20.5937, 78.9629], // Center of India
      zoom: 4,
      zoomControl: true,
    });

    // 100% Free OpenStreetMap tile layer - zero external API key required
    const osmTiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    });

    osmTiles.addTo(state.map);

    updateBoundingBoxOnMap();
  }

  function updateBoundingBoxOnMap() {
    if (!state.map) return;

    if (state.bboxLayer) {
      state.map.removeLayer(state.bboxLayer);
      state.bboxLayer = null;
    }

    if (!state.rules.checkBoundingBox) return;

    const bounds = [
      [state.rules.bboxMinLat, state.rules.bboxMinLon],
      [state.rules.bboxMaxLat, state.rules.bboxMaxLon]
    ];

    state.bboxLayer = L.rectangle(bounds, {
      color: '#06b6d4',
      weight: 1.5,
      dashArray: '4, 4',
      fillColor: '#06b6d4',
      fillOpacity: 0.06
    }).addTo(state.map);

    state.bboxLayer.bindTooltip(`Bounding Box: ${state.rules.bboxRegion}`, {
      direction: 'top',
      opacity: 0.8
    });
  }

  // ==================== BACKEND HEALTH CHECK ====================
  async function checkBackendHealth() {
    try {
      const res = await fetch('/api/health');
      if (res.ok) {
        state.backendOnline = true;
        elements.backendDot.classList.add('online');
        elements.backendText.textContent = 'Validator Engine: Active';
      } else {
        throw new Error('Non-200 response');
      }
    } catch (e) {
      state.backendOnline = false;
      elements.backendDot.classList.add('online');
      elements.backendText.textContent = 'Validator Engine: Active (Local)';
    }
  }

  // ==================== EVENT LISTENERS ====================
  function initEventListeners() {
    // Dropdown for export
    elements.btnExportDropdown.addEventListener('click', (e) => {
      e.stopPropagation();
      elements.btnExportDropdown.parentElement.classList.toggle('open');
    });

    document.addEventListener('click', () => {
      elements.btnExportDropdown.parentElement.classList.remove('open');
    });

    // Export items
    document.querySelectorAll('.dropdown-item').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const exportType = item.getAttribute('data-export');
        triggerExport(exportType);
      });
    });

    // Format toggle tabs
    elements.tabCsv.addEventListener('click', () => switchFormat('csv'));
    elements.tabJson.addEventListener('click', () => switchFormat('json'));

    // Sample loading chips
    elements.chipCsv.addEventListener('click', () => {
      loadDataContent(SAMPLE_CSV, 'csv');
      executeValidation();
      showToast('Loaded India Regional CSV dataset', 'info');
    });

    elements.chipJson.addEventListener('click', () => {
      loadDataContent(SAMPLE_JSON, 'json');
      executeValidation();
      showToast('Loaded Global JSON dataset', 'info');
    });

    elements.btnQuickSample.addEventListener('click', () => {
      loadDataContent(SAMPLE_CSV, 'csv');
      executeValidation();
      showToast('Loaded Sample Dataset', 'info');
    });

    // Textarea input monitoring
    elements.rawDataInput.addEventListener('input', () => {
      state.rawContent = elements.rawDataInput.value;
      updateLineCountBadge();
    });

    // Validate button
    elements.btnValidate.addEventListener('click', () => {
      state.rawContent = elements.rawDataInput.value;
      executeValidation();
    });

    // File Dropzone & Browse
    elements.btnBrowseFile.addEventListener('click', () => elements.fileInput.click());
    elements.fileDropzone.addEventListener('click', (e) => {
      if (e.target !== elements.btnBrowseFile) elements.fileInput.click();
    });

    elements.fileDropzone.addEventListener('dragover', (e) => {
      e.preventDefault();
      elements.fileDropzone.classList.add('dragover');
    });

    elements.fileDropzone.addEventListener('dragleave', () => {
      elements.fileDropzone.classList.remove('dragover');
    });

    elements.fileDropzone.addEventListener('drop', (e) => {
      e.preventDefault();
      elements.fileDropzone.classList.remove('dragover');
      if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        handleFileSelect(e.dataTransfer.files[0]);
      }
    });

    elements.fileInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        handleFileSelect(e.target.files[0]);
      }
    });

    // Search filter
    elements.tableSearchInput.addEventListener('input', (e) => {
      state.searchQuery = e.target.value.toLowerCase().trim();
      renderResultsTable();
    });

    // Category filter tabs
    elements.filterTabs.forEach(tab => {
      tab.addEventListener('click', () => {
        elements.filterTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        state.activeFilter = tab.getAttribute('data-filter');
        renderResultsTable();
      });
    });

    // Map controls
    elements.btnFitBounds.addEventListener('click', fitMapBounds);
    elements.btnToggleBBox.addEventListener('click', () => {
      if (state.bboxLayer) {
        if (state.map.hasLayer(state.bboxLayer)) {
          state.map.removeLayer(state.bboxLayer);
          elements.btnToggleBBox.classList.remove('active');
        } else {
          state.bboxLayer.addTo(state.map);
          elements.btnToggleBBox.classList.add('active');
        }
      }
    });

    // Settings Modal
    elements.btnOpenSettings.addEventListener('click', () => {
      syncSettingsToModal();
      elements.settingsModal.classList.add('open');
    });
    elements.btnCloseSettings.addEventListener('click', () => {
      elements.settingsModal.classList.remove('open');
    });
    elements.settingsModal.addEventListener('click', (e) => {
      if (e.target === elements.settingsModal) elements.settingsModal.classList.remove('open');
    });

    // Sliders
    elements.inputIqrMultiplier.addEventListener('input', (e) => {
      elements.valIqrMultiplier.textContent = e.target.value;
    });
    elements.inputPrecisionPlaces.addEventListener('input', (e) => {
      elements.valPrecisionPlaces.textContent = e.target.value;
    });

    // Presets
    elements.presetButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        elements.presetButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const p = PRESETS[btn.getAttribute('data-preset')];
        if (p) {
          elements.bboxRegion.value = p.name;
          elements.bboxMinLat.value = p.minLat;
          elements.bboxMaxLat.value = p.maxLat;
          elements.bboxMinLon.value = p.minLon;
          elements.bboxMaxLon.value = p.maxLon;
        }
      });
    });

    // Save Rules
    elements.btnSaveRules.addEventListener('click', () => {
      saveSettingsFromModal();
      elements.settingsModal.classList.remove('open');
      updateBoundingBoxOnMap();
      executeValidation();
      showToast('Validation rules applied', 'success');
    });

    // Reset Rules
    elements.btnResetRules.addEventListener('click', () => {
      elements.ruleFormat.checked = true;
      elements.ruleRange.checked = true;
      elements.ruleDuplicates.checked = true;
      elements.ruleOutliers.checked = true;
      elements.rulePrecision.checked = true;
      elements.ruleBBox.checked = true;
      elements.inputIqrMultiplier.value = 1.5;
      elements.valIqrMultiplier.textContent = '1.5';
      elements.inputPrecisionPlaces.value = 6;
      elements.valPrecisionPlaces.textContent = '6';

      const p = PRESETS.india;
      elements.bboxRegion.value = p.name;
      elements.bboxMinLat.value = p.minLat;
      elements.bboxMaxLat.value = p.maxLat;
      elements.bboxMinLon.value = p.minLon;
      elements.bboxMaxLon.value = p.maxLon;
    });
  }

  // ==================== FORMAT & FILE HANDLING ====================
  function switchFormat(newFormat) {
    state.format = newFormat;
    if (newFormat === 'csv') {
      elements.tabCsv.classList.add('active');
      elements.tabJson.classList.remove('active');
      elements.rawDataInput.placeholder = 'label,latitude,longitude\nDelhi,28.6139,77.2090';
    } else {
      elements.tabJson.classList.add('active');
      elements.tabCsv.classList.remove('active');
      elements.rawDataInput.placeholder = '[\n  {"label": "Delhi", "latitude": 28.6139, "longitude": 77.2090}\n]';
    }
  }

  function loadDataContent(content, format) {
    state.rawContent = content;
    elements.rawDataInput.value = content;
    switchFormat(format);
    updateLineCountBadge();
  }

  function updateLineCountBadge() {
    const lines = state.rawContent.trim().split(/\r\n|\r|\n/).length;
    elements.rawLinesBadge.textContent = `${lines} ${lines === 1 ? 'line' : 'lines'}`;
  }

  function handleFileSelect(file) {
    const name = file.name.toLowerCase();
    const reader = new FileReader();

    reader.onload = (e) => {
      const content = e.target.result;
      if (name.endsWith('.json')) {
        loadDataContent(content, 'json');
      } else {
        loadDataContent(content, 'csv');
      }
      executeValidation();
      showToast(`Loaded ${file.name}`, 'success');
    };

    reader.onerror = () => {
      showToast(`Failed to read file ${file.name}`, 'error');
    };

    reader.readAsText(file);
  }

  // ==================== SETTINGS MODAL SYNC ====================
  function syncSettingsToModal() {
    elements.ruleFormat.checked = state.rules.checkFormat;
    elements.ruleRange.checked = state.rules.checkRange;
    elements.ruleDuplicates.checked = state.rules.checkDuplicates;
    elements.ruleOutliers.checked = state.rules.checkOutliers;
    elements.rulePrecision.checked = state.rules.checkPrecision;
    elements.ruleBBox.checked = state.rules.checkBoundingBox;

    elements.inputIqrMultiplier.value = state.rules.outlierIqr;
    elements.valIqrMultiplier.textContent = state.rules.outlierIqr;
    elements.inputPrecisionPlaces.value = state.rules.precisionPlaces;
    elements.valPrecisionPlaces.textContent = state.rules.precisionPlaces;

    elements.bboxRegion.value = state.rules.bboxRegion;
    elements.bboxMinLat.value = state.rules.bboxMinLat;
    elements.bboxMaxLat.value = state.rules.bboxMaxLat;
    elements.bboxMinLon.value = state.rules.bboxMinLon;
    elements.bboxMaxLon.value = state.rules.bboxMaxLon;
  }

  function saveSettingsFromModal() {
    state.rules.checkFormat = elements.ruleFormat.checked;
    state.rules.checkRange = elements.ruleRange.checked;
    state.rules.checkDuplicates = elements.ruleDuplicates.checked;
    state.rules.checkOutliers = elements.ruleOutliers.checked;
    state.rules.checkPrecision = elements.rulePrecision.checked;
    state.rules.checkBoundingBox = elements.ruleBBox.checked;

    state.rules.outlierIqr = parseFloat(elements.inputIqrMultiplier.value) || 1.5;
    state.rules.precisionPlaces = parseInt(elements.inputPrecisionPlaces.value, 10) || 6;

    state.rules.bboxRegion = elements.bboxRegion.value.trim() || 'Custom Region';
    state.rules.bboxMinLat = parseFloat(elements.bboxMinLat.value) || 6.0;
    state.rules.bboxMaxLat = parseFloat(elements.bboxMaxLat.value) || 37.5;
    state.rules.bboxMinLon = parseFloat(elements.bboxMinLon.value) || 68.0;
    state.rules.bboxMaxLon = parseFloat(elements.bboxMaxLon.value) || 97.5;
  }

  // ==================== EXECUTE VALIDATION ====================
  async function executeValidation() {
    if (!state.rawContent.trim()) {
      showToast('Please enter or upload coordinates to validate.', 'error');
      return;
    }

    elements.btnValidate.disabled = true;
    elements.btnValidate.innerHTML = `
      <svg class="spin-icon" viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2.2" fill="none"><circle cx="12" cy="12" r="10"></circle><path d="M12 2a10 10 0 0 1 10 10"></path></svg>
      Validating...
    `;

    try {
      if (state.backendOnline) {
        // Run validation against Java REST API
        const payload = {
          format: state.format,
          data: state.rawContent,
          options: state.rules
        };

        const response = await fetch('/api/validate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (!response.ok) {
          const errData = await response.json();
          throw new Error(errData.error || 'Server validation error');
        }

        const data = await response.json();
        state.results = data.results || [];
        state.summary = data.summary || {};
      } else {
        // Standalone client-side fallback validation engine
        runClientSideValidation();
      }

      updateKPICards();
      updateMapMarkers();
      renderResultsTable();
      showToast(`Validation complete: ${state.summary.validCount} valid / ${state.summary.totalCount} total`, 'success');

    } catch (err) {
      state.backendOnline = false;
      elements.backendDot.classList.add('online');
      elements.backendText.textContent = 'Validator Engine: Active (Local)';

      runClientSideValidation();
      updateKPICards();
      updateMapMarkers();
      renderResultsTable();
      showToast(`Validation complete: ${state.summary.validCount} valid / ${state.summary.totalCount} total`, 'success');
    } finally {
      elements.btnValidate.disabled = false;
      elements.btnValidate.innerHTML = `
        <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" stroke-width="2.2" fill="none"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        Execute Validation Suite
      `;
    }
  }

  // ==================== CLIENT-SIDE ENGINE (FALLBACK) ====================
  function runClientSideValidation() {
    let rawItems = [];

    if (state.format === 'csv') {
      const lines = state.rawContent.trim().split(/\r\n|\r|\n/);
      if (lines.length > 0) {
        const header = lines[0].toLowerCase().split(',');
        let latIdx = header.findIndex(h => h.includes('lat'));
        let lonIdx = header.findIndex(h => h.includes('lon') || h.includes('lng'));
        let labelIdx = header.findIndex(h => h.includes('label') || h.includes('id') || h.includes('name'));

        if (latIdx === -1) latIdx = 1;
        if (lonIdx === -1) lonIdx = 2;
        if (labelIdx === -1) labelIdx = 0;

        for (let i = 1; i < lines.length; i++) {
          const row = lines[i].trim();
          if (!row) continue;
          const cols = row.split(',').map(c => c.trim());
          rawItems.push({
            rowNumber: i,
            label: cols[labelIdx] || `Row_${i}`,
            rawLat: cols[latIdx] || '',
            rawLon: cols[lonIdx] || '',
          });
        }
      }
    } else {
      try {
        const parsed = JSON.parse(state.rawContent);
        const array = Array.isArray(parsed) ? parsed : (parsed.coordinates || parsed.data || []);
        rawItems = array.map((item, idx) => ({
          rowNumber: idx + 1,
          label: item.label || item.id || item.name || `Row_${idx + 1}`,
          rawLat: item.latitude !== undefined && item.latitude !== null ? String(item.latitude) : '',
          rawLon: item.longitude !== undefined && item.longitude !== null ? String(item.longitude) : '',
        }));
      } catch (e) {
        showToast('JSON parse error: ' + e.message, 'error');
        return;
      }
    }

    // Parse and apply rules
    const coordinates = rawItems.map(item => {
      const latMissing = !item.rawLat || item.rawLat.trim() === '';
      const lonMissing = !item.rawLon || item.rawLon.trim() === '';
      const latNum = parseFloat(item.rawLat);
      const lonNum = parseFloat(item.rawLon);
      const latBad = !latMissing && isNaN(latNum);
      const lonBad = !lonMissing && isNaN(lonNum);

      return {
        rowNumber: item.rowNumber,
        label: item.label,
        rawLatitude: item.rawLat,
        rawLongitude: item.rawLon,
        latitude: !latMissing && !latBad ? latNum : null,
        longitude: !lonMissing && !lonBad ? lonNum : null,
        isParseable: !latMissing && !lonMissing && !latBad && !lonBad,
        latMissing,
        lonMissing,
        latBad,
        lonBad
      };
    });

    // Rule validation
    const results = [];
    const seenCoordinates = new Map();

    // Compute IQR outliers for parseable coordinates
    const parseable = coordinates.filter(c => c.isParseable);
    const outlierRows = new Set();

    if (state.rules.checkOutliers && parseable.length >= 4) {
      const getIQRBounds = (values) => {
        const sorted = [...values].sort((a, b) => a - b);
        const q1 = sorted[Math.floor(sorted.length * 0.25)];
        const q3 = sorted[Math.floor(sorted.length * 0.75)];
        const iqr = q3 - q1;
        const k = state.rules.outlierIqr;
        return { min: q1 - k * iqr, max: q3 + k * iqr };
      };

      const latBounds = getIQRBounds(parseable.map(c => c.latitude));
      const lonBounds = getIQRBounds(parseable.map(c => c.longitude));

      parseable.forEach(c => {
        if (c.latitude < latBounds.min || c.latitude > latBounds.max ||
            c.longitude < lonBounds.min || c.longitude > lonBounds.max) {
          outlierRows.add(c.rowNumber);
        }
      });
    }

    // First pass for duplicates
    coordinates.forEach(c => {
      if (c.isParseable) {
        const key = `${c.latitude.toFixed(6)},${c.longitude.toFixed(6)}`;
        seenCoordinates.set(key, (seenCoordinates.get(key) || 0) + 1);
      }
    });

    coordinates.forEach(c => {
      const errors = [];
      let category = 'valid';

      // 1. Format check
      if (state.rules.checkFormat) {
        if (c.latMissing) errors.push('Missing latitude value');
        if (c.lonMissing) errors.push('Missing longitude value');
        if (c.latBad) errors.push(`Invalid latitude format: '${c.rawLatitude}'`);
        if (c.lonBad) errors.push(`Invalid longitude format: '${c.rawLongitude}'`);
      }

      // 2. Range check
      if (state.rules.checkRange && c.isParseable) {
        if (c.latitude < -90 || c.latitude > 90) {
          errors.push(`Latitude ${c.latitude.toFixed(4)} out of valid range [-90, 90]`);
        }
        if (c.longitude < -180 || c.longitude > 180) {
          errors.push(`Longitude ${c.longitude.toFixed(4)} out of valid range [-180, 180]`);
        }
      }

      // 3. Duplicate check
      if (state.rules.checkDuplicates && c.isParseable) {
        const key = `${c.latitude.toFixed(6)},${c.longitude.toFixed(6)}`;
        if (seenCoordinates.get(key) > 1) {
          errors.push(`Duplicate coordinate detected (${c.latitude.toFixed(4)}, ${c.longitude.toFixed(4)})`);
        }
      }

      // 4. Outlier check
      if (state.rules.checkOutliers && outlierRows.has(c.rowNumber)) {
        errors.push(`Statistical spatial outlier detected by IQR rule`);
      }

      // 5. Precision check
      if (state.rules.checkPrecision && c.isParseable) {
        const getDecimals = (str) => {
          const parts = str.split('.');
          return parts.length > 1 ? parts[1].length : 0;
        };
        const maxP = state.rules.precisionPlaces;
        if (getDecimals(c.rawLatitude) > maxP || getDecimals(c.rawLongitude) > maxP) {
          errors.push(`Precision exceeds maximum threshold of ${maxP} decimal places`);
        }
      }

      // 6. Bounding box check
      if (state.rules.checkBoundingBox && c.isParseable) {
        const { bboxMinLat, bboxMaxLat, bboxMinLon, bboxMaxLon, bboxRegion } = state.rules;
        if (c.latitude < bboxMinLat || c.latitude > bboxMaxLat ||
            c.longitude < bboxMinLon || c.longitude > bboxMaxLon) {
          errors.push(`Coordinates outside ${bboxRegion} bounds`);
        }
      }

      const isValid = errors.length === 0;

      // Determine primary categorization
      if (!isValid) {
        const errStr = errors.join(' ').toLowerCase();
        if (errStr.includes('missing') || errStr.includes('invalid') || errStr.includes('format')) category = 'format';
        else if (errStr.includes('range [-')) category = 'range';
        else if (errStr.includes('duplicate')) category = 'duplicate';
        else if (errStr.includes('outlier')) category = 'outlier';
        else if (errStr.includes('bounds')) category = 'bbox';
        else category = 'invalid';
      }

      results.push({
        rowNumber: c.rowNumber,
        label: c.label,
        latitude: c.latitude,
        longitude: c.longitude,
        rawLatitude: c.rawLatitude,
        rawLongitude: c.rawLongitude,
        isValid,
        status: isValid ? 'VALID' : 'INVALID',
        errors,
        formattedErrors: errors.join('; '),
        category
      });
    });

    state.results = results;
    const totalCount = results.length;
    const validCount = results.filter(r => r.isValid).length;
    const invalidCount = totalCount - validCount;
    const passRate = totalCount > 0 ? (validCount * 100.0 / totalCount) : 0;

    state.summary = {
      totalCount,
      validCount,
      invalidCount,
      passRate: Math.round(passRate * 10) / 10,
      duplicateCount: results.filter(r => r.category === 'duplicate').length,
      outlierCount: results.filter(r => r.category === 'outlier').length,
      bboxBreachCount: results.filter(r => r.category === 'bbox').length,
    };
  }

  // ==================== KPI STATS RENDERING ====================
  function updateKPICards() {
    elements.kpiTotal.textContent = state.summary.totalCount || 0;
    elements.kpiValid.textContent = state.summary.validCount || 0;
    elements.kpiInvalid.textContent = state.summary.invalidCount || 0;
    elements.kpiOutliers.textContent = state.summary.outlierCount || 0;
    elements.kpiDuplicates.textContent = state.summary.duplicateCount || 0;
    elements.kpiRateText.textContent = `${state.summary.passRate || 0}% Clean Rate`;

    // Filter tab badge counts
    document.getElementById('count-all').textContent = state.summary.totalCount || 0;
    document.getElementById('count-valid').textContent = state.summary.validCount || 0;
    document.getElementById('count-invalid').textContent = state.summary.invalidCount || 0;
    document.getElementById('count-outlier').textContent = state.summary.outlierCount || 0;
    document.getElementById('count-duplicate').textContent = state.summary.duplicateCount || 0;
    document.getElementById('count-bbox').textContent = state.summary.bboxBreachCount || 0;
  }

  // ==================== MAP MARKERS RENDERING ====================
  function updateMapMarkers() {
    if (!state.map) return;

    // Clear existing markers
    state.markers.forEach(m => state.map.removeLayer(m));
    state.markers = [];

    const bounds = [];
    let mappedCount = 0;

    state.results.forEach((item, index) => {
      if (item.latitude === null || item.longitude === null) return;
      // Also ignore wild coordinates outside real sphere bounds for Leaflet stability
      if (item.latitude < -90 || item.latitude > 90 || item.longitude < -180 || item.longitude > 180) {
        return;
      }

      mappedCount++;
      bounds.push([item.latitude, item.longitude]);

      let pinColorClass = 'pin-valid';
      if (!item.isValid) {
        if (item.category === 'outlier') pinColorClass = 'pin-outlier';
        else if (item.category === 'duplicate') pinColorClass = 'pin-duplicate';
        else if (item.category === 'bbox') pinColorClass = 'pin-bbox';
        else pinColorClass = 'pin-invalid';
      }

      const customIcon = L.divIcon({
        className: 'custom-pin',
        html: `<div class="pin-inner ${pinColorClass}"></div>`,
        iconSize: [16, 16],
        iconAnchor: [8, 8]
      });

      const marker = L.marker([item.latitude, item.longitude], { icon: customIcon });

      const popupHtml = `
        <div class="map-popup-body">
          <div class="popup-title">
            <span>#${item.rowNumber} ${escapeHtml(item.label)}</span>
            <span class="badge ${item.isValid ? 'badge-valid' : 'badge-invalid'}">${item.status}</span>
          </div>
          <div class="popup-coords">${item.latitude.toFixed(6)}, ${item.longitude.toFixed(6)}</div>
          ${item.isValid ? '' : `<div class="popup-reasons">${escapeHtml(item.formattedErrors)}</div>`}
        </div>
      `;

      marker.bindPopup(popupHtml);

      // On marker click: highlight row in table
      marker.on('click', () => {
        highlightTableRow(item.rowNumber);
      });

      marker.addTo(state.map);
      marker.datasetRowNumber = item.rowNumber;
      state.markers.push(marker);
    });

    elements.mapPointCounter.textContent = `${mappedCount} Points Mapped`;

    // Adjust map zoom/view
    if (bounds.length > 0) {
      state.map.fitBounds(bounds, { padding: [50, 50], maxZoom: 10 });
    }
  }

  function fitMapBounds() {
    if (!state.map || state.markers.length === 0) return;
    const group = L.featureGroup(state.markers);
    state.map.fitBounds(group.getBounds().pad(0.1));
  }

  // ==================== TABLE RENDERING ====================
  function renderResultsTable() {
    let filtered = state.results;

    // Filter by active category tab
    if (state.activeFilter === 'valid') {
      filtered = filtered.filter(r => r.isValid);
    } else if (state.activeFilter === 'invalid') {
      filtered = filtered.filter(r => !r.isValid);
    } else if (state.activeFilter === 'outlier') {
      filtered = filtered.filter(r => r.category === 'outlier');
    } else if (state.activeFilter === 'duplicate') {
      filtered = filtered.filter(r => r.category === 'duplicate');
    } else if (state.activeFilter === 'bbox') {
      filtered = filtered.filter(r => r.category === 'bbox');
    }

    // Filter by search query
    if (state.searchQuery) {
      const q = state.searchQuery;
      filtered = filtered.filter(r =>
        String(r.rowNumber).includes(q) ||
        r.label.toLowerCase().includes(q) ||
        (r.rawLatitude && r.rawLatitude.toLowerCase().includes(q)) ||
        (r.rawLongitude && r.rawLongitude.toLowerCase().includes(q)) ||
        (r.formattedErrors && r.formattedErrors.toLowerCase().includes(q))
      );
    }

    if (filtered.length === 0) {
      elements.resultsTbody.innerHTML = `
        <tr class="empty-row">
          <td colspan="7">
            <div class="empty-state">
              <svg viewBox="0 0 24 24" width="40" height="40" stroke="currentColor" stroke-width="1.5" fill="none"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
              <p>No matching coordinate records found for current filter.</p>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    const rowsHtml = filtered.map(item => {
      const statusBadge = item.isValid
        ? `<span class="badge badge-valid">VALID</span>`
        : `<span class="badge badge-invalid">INVALID</span>`;

      const reasonsHtml = item.isValid
        ? `<span style="color: var(--text-muted);">-</span>`
        : `<div class="failure-reasons">${item.errors.map(err => `<span class="reason-tag">⚠️ ${escapeHtml(err)}</span>`).join('')}</div>`;

      const latDisplay = item.rawLatitude ? escapeHtml(item.rawLatitude) : '<span style="color:#f87171">&lt;MISSING&gt;</span>';
      const lonDisplay = item.rawLongitude ? escapeHtml(item.rawLongitude) : '<span style="color:#f87171">&lt;MISSING&gt;</span>';

      return `
        <tr id="row-item-${item.rowNumber}" data-row="${item.rowNumber}">
          <td class="font-mono text-muted">#${item.rowNumber}</td>
          <td><strong>${escapeHtml(item.label)}</strong></td>
          <td class="row-coord">${latDisplay}</td>
          <td class="row-coord">${lonDisplay}</td>
          <td>${statusBadge}</td>
          <td>${reasonsHtml}</td>
          <td class="text-center">
            ${item.latitude !== null && item.longitude !== null
              ? `<button class="btn-focus-coord" onclick="window.focusPoint(${item.rowNumber}, ${item.latitude}, ${item.longitude})" title="Zoom to marker on map">Focus 🔍</button>`
              : `<span class="text-muted" style="font-size:0.75rem">No Fix</span>`}
          </td>
        </tr>
      `;
    }).join('');

    elements.resultsTbody.innerHTML = rowsHtml;
  }

  function highlightTableRow(rowNumber) {
    document.querySelectorAll('.data-table tbody tr').forEach(r => r.classList.remove('row-selected'));
    const target = document.getElementById(`row-item-${rowNumber}`);
    if (target) {
      target.classList.add('row-selected');
      target.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  // Global helper exposed to inline button onclick
  window.focusPoint = function (rowNumber, lat, lon) {
    highlightTableRow(rowNumber);
    if (state.map) {
      state.map.flyTo([lat, lon], 12, { duration: 1.2 });
      const marker = state.markers.find(m => m.datasetRowNumber === rowNumber);
      if (marker) {
        setTimeout(() => marker.openPopup(), 1200);
      }
    }
  };

  // ==================== EXPORT SYSTEM ====================
  async function triggerExport(type) {
    if (state.results.length === 0) {
      showToast('No validated results to export. Run validation first.', 'error');
      return;
    }

    try {
      if (state.backendOnline) {
        // Delegate to Java backend ReportExporter implementations
        const response = await fetch('/api/export', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            data: state.rawContent,
            format: state.format,
            exportType: type,
            options: state.rules
          })
        });

        if (!response.ok) throw new Error('Backend export failed');

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `validation_report.${type}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        showToast(`Downloaded validation report as .${type}`, 'success');
      } else {
        // Client-side fallback export
        let content = '';
        let mime = 'text/plain';

        if (type === 'json') {
          content = JSON.stringify({ summary: state.summary, results: state.results }, null, 2);
          mime = 'application/json';
        } else if (type === 'csv') {
          content = 'row_number,label,latitude,longitude,status,errors\n' +
            state.results.map(r => `"${r.rowNumber}","${r.label}","${r.rawLatitude}","${r.rawLongitude}","${r.status}","${r.formattedErrors}"`).join('\n');
          mime = 'text/csv';
        } else if (type === 'txt') {
          content = `===== VALIDATION REPORT =====\nTotal: ${state.summary.totalCount} | Valid: ${state.summary.validCount} | Invalid: ${state.summary.invalidCount}\n\n` +
            state.results.map(r => `[${r.status}] #${r.rowNumber} ${r.label} (${r.rawLatitude}, ${r.rawLongitude}) -> ${r.formattedErrors || 'None'}`).join('\n');
        } else {
          content = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>Validation Report</title><style>body{font-family:sans-serif;margin:30px;background:#f8fafc;color:#1e293b}table{width:100%;border-collapse:collapse;margin-top:20px}th,td{padding:10px;border-bottom:1px solid #cbd5e1;text-align:left}th{background:#e2e8f0}.valid{color:green}.invalid{color:red}</style></head><body><h1>Validation Report</h1><p>Total: ${state.summary.totalCount} | Valid: ${state.summary.validCount} | Invalid: ${state.summary.invalidCount}</p><table><thead><tr><th>#</th><th>Label</th><th>Latitude</th><th>Longitude</th><th>Status</th><th>Errors</th></tr></thead><tbody>` +
            state.results.map(r => `<tr><td>${r.rowNumber}</td><td>${r.label}</td><td>${r.rawLatitude}</td><td>${r.rawLongitude}</td><td class="${r.isValid ? 'valid' : 'invalid'}">${r.status}</td><td>${r.formattedErrors}</td></tr>`).join('') +
            `</tbody></table></body></html>`;
          mime = 'text/html';
        }

        const blob = new Blob([content], { type: mime });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `validation_report.${type}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        showToast(`Generated and downloaded .${type} report`, 'success');
      }
    } catch (e) {
      showToast('Export error: ' + e.message, 'error');
    }
  }

  // ==================== TOAST HELPER ====================
  function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 250ms ease';
      setTimeout(() => toast.remove(), 250);
    }, 3200);
  }

  function escapeHtml(text) {
    if (!text) return '';
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Boot on DOM ready
  document.addEventListener('DOMContentLoaded', init);

})();
