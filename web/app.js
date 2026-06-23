const app = {
    role: null,
    activeScreen: 'auth',
    threeJsScene: null,

    init() {
        // Setup search listener
        document.getElementById('nav-search-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                app.performSearch();
            }
        });
        
        // Setup scroll telemetry for AI Chat
        const chatLog = document.getElementById('ai-chat-log');
        if (chatLog) {
            chatLog.scrollTop = chatLog.scrollHeight;
        }

        // Parallax hover effect for cards
        document.addEventListener('mousemove', (e) => {
            const x = (window.innerWidth / 2 - e.pageX) / 45;
            const y = (window.innerHeight / 2 - e.pageY) / 45;
            const floatCards = document.querySelectorAll('.animate-float');
            floatCards.forEach(card => {
                card.style.transform = `translate(${x}px, ${y}px)`;
            });
        });
    },

    toggleAuthForm(type) {
        const loginForm = document.getElementById('login-form');
        const regForm = document.getElementById('register-form');
        const loginTab = document.getElementById('auth-tab-login');
        const regTab = document.getElementById('auth-tab-register');

        if (type === 'login') {
            loginForm.classList.remove('hidden');
            regForm.classList.add('hidden');
            loginTab.className = "flex-1 py-2 font-button text-xs rounded-lg bg-surface-container-high text-white transition-all";
            regTab.className = "flex-1 py-2 font-button text-xs rounded-lg text-on-surface-variant hover:text-white transition-all";
        } else {
            loginForm.classList.add('hidden');
            regForm.classList.remove('hidden');
            regTab.className = "flex-1 py-2 font-button text-xs rounded-lg bg-surface-container-high text-white transition-all";
            loginTab.className = "flex-1 py-2 font-button text-xs rounded-lg text-on-surface-variant hover:text-white transition-all";
        }
    },

    showScreen(screenId) {
        this.activeScreen = screenId;
        const screens = ['auth', 'dashboard', 'feed', 'shop-ai', 'wallet-track', 'admin'];
        screens.forEach(s => {
            const el = document.getElementById(`screen-${s}`);
            if (el) el.classList.add('hidden');
        });

        // Toggle layout shells
        if (screenId !== 'auth') {
            document.getElementById('top-nav').classList.remove('hidden');
            document.getElementById('side-nav').classList.remove('hidden');
            document.getElementById('main-content-shell').classList.add('lg:pl-64');
        } else {
            document.getElementById('top-nav').classList.add('hidden');
            document.getElementById('side-nav').classList.add('hidden');
            document.getElementById('main-content-shell').classList.remove('lg:pl-64');
        }

        const activeScreenEl = document.getElementById(`screen-${screenId}`);
        if (activeScreenEl) activeScreenEl.classList.remove('hidden');

        // Sync header tabs active status
        document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active-nav-line', 'text-primary'));
        if (screenId === 'dashboard') {
            document.getElementById('tab-curated').classList.add('active-nav-line', 'text-primary');
            this.performSearch();
            this.loadRecentSearches();
        } else if (screenId === 'shop-ai') {
            document.getElementById('tab-ai').classList.add('active-nav-line', 'text-primary');
            this.initThreeJs();
        } else if (screenId === 'wallet-track') {
            document.getElementById('tab-wallet').classList.add('active-nav-line', 'text-primary');
        } else if (screenId === 'admin') {
            const adminTab = document.getElementById('tab-admin');
            if (adminTab) adminTab.classList.add('active-nav-line', 'text-primary');
            this.loadStats();
        }

        // Sync sidebar active status
        document.querySelectorAll('.sidebar-item').forEach(item => {
            item.className = "sidebar-item flex items-center gap-4 text-on-surface-variant pl-8 py-3 hover:bg-white/5 hover:text-white transition-all";
        });
        const activeSideBtn = document.getElementById(`side-${screenId === 'dashboard' ? 'discover' : screenId === 'shop-ai' ? 'ai' : screenId}`);
        if (activeSideBtn) {
            activeSideBtn.className = "sidebar-item flex items-center gap-4 text-primary bg-primary/10 border-l-4 border-primary pl-7 py-3 transition-all";
        }

        // In case of screen-feed, load collections
        if (screenId === 'feed') {
            this.loadCollections();
        }
    },

    async login() {
        const user = document.getElementById('login-user').value;
        const pass = document.getElementById('login-pass').value;
        const errorEl = document.getElementById('login-error');
        
        try {
            const body = new URLSearchParams();
            body.append('username', user);
            body.append('password', pass);

            const res = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            });

            if (res.ok) {
                const data = await res.json();
                this.role = data.role;
                errorEl.classList.add('hidden');
                
                if (this.role === 'admin') {
                    document.getElementById('nav-admin-btn').classList.remove('hidden');
                    document.getElementById('side-admin-btn').classList.remove('hidden');
                } else {
                    document.getElementById('nav-admin-btn').classList.add('hidden');
                    document.getElementById('side-admin-btn').classList.add('hidden');
                }
                
                this.showScreen('dashboard');
            } else {
                errorEl.textContent = 'Invalid credentials. Access Denied.';
                errorEl.classList.remove('hidden');
            }
        } catch (e) {
            console.error(e);
            errorEl.textContent = 'System Error. Try again.';
            errorEl.classList.remove('hidden');
        }
    },

    async register() {
        const user = document.getElementById('reg-user').value;
        const email = document.getElementById('reg-email').value;
        const pass = document.getElementById('reg-pass').value;
        const budget = document.getElementById('reg-budget').value;
        const diet = document.getElementById('reg-diet').value;
        const errorEl = document.getElementById('register-error');

        try {
            const body = new URLSearchParams();
            body.append('username', user);
            body.append('email', email);
            body.append('password', pass);
            body.append('budget', budget);
            body.append('diet', diet);

            const res = await fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            });

            if (res.ok) {
                errorEl.classList.add('hidden');
                alert('Registration successful! Please login.');
                this.toggleAuthForm('login');
            } else {
                const data = await res.json();
                errorEl.textContent = data.message || 'Registration failed.';
                errorEl.classList.remove('hidden');
            }
        } catch (e) {
            console.error(e);
            errorEl.textContent = 'System Error. Try again.';
            errorEl.classList.remove('hidden');
        }
    },

    async logout() {
        await fetch('/api/logout', { method: 'POST' });
        this.role = null;
        document.getElementById('login-user').value = '';
        document.getElementById('login-pass').value = '';
        this.showScreen('auth');
    },

    async performSearch(queryOverride = null) {
        const query = queryOverride !== null ? queryOverride : document.getElementById('nav-search-input').value;
        if (queryOverride !== null) {
            document.getElementById('nav-search-input').value = query;
        }
        
        const domain = document.getElementById('domain-filter').value;
        
        try {
            const res = await fetch(`/api/recommendations?q=${encodeURIComponent(query)}&domain=${encodeURIComponent(domain)}`);
            const items = await res.json();
            this.renderItems(items);
            this.loadRecentSearches();
        } catch (e) {
            console.error(e);
        }
    },

    async loadRecentSearches() {
        try {
            const res = await fetch('/api/recent_searches');
            const searches = await res.json();
            const container = document.getElementById('recent-searches');
            container.innerHTML = '';
            
            if (searches.length > 0) {
                const titleLabel = document.createElement('span');
                titleLabel.className = 'text-xs font-label-mono text-outline-variant mr-2';
                titleLabel.textContent = 'RECENT SEARCHES:';
                container.appendChild(titleLabel);
            }

            searches.forEach(s => {
                const badge = document.createElement('button');
                badge.className = 'px-3 py-1 bg-surface-container-high/60 border border-outline-variant/30 rounded-full text-[10px] font-label-mono text-on-surface hover:text-primary hover:border-primary/50 transition-colors';
                badge.textContent = s;
                badge.onclick = () => this.performSearch(s);
                container.appendChild(badge);
            });
        } catch (e) {
            console.error(e);
        }
    },

    getItemImage(item) {
        if (item.image_path && item.image_path.trim().length > 4) {
            return item.image_path;
        }
        // Premium fallback pictures by domain categories matching deep space aesthetics
        const category = (item.domain_category || '').toLowerCase();
        if (category.includes('elect') || category.includes('tech')) {
            return 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=400&q=80'; // Watch
        } else if (category.includes('shop') || category.includes('product') || category.includes('shoe')) {
            return 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=400&q=80'; // Red sneaker
        } else if (category.includes('food') || category.includes('din')) {
            return 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=400&q=80'; // Ramen
        } else if (category.includes('entert') || category.includes('media') || category.includes('game')) {
            return 'https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&w=400&q=80'; // Gaming Setup
        }
        return 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=400&q=80'; // Abstract geometric art
    },

    renderItems(items) {
        const grid = document.getElementById('results-grid');
        grid.innerHTML = '';
        
        items.forEach(item => {
            const card = document.createElement('div');
            card.className = 'glass-card rounded-3xl overflow-hidden flex flex-col group relative solar-border';
            
            const imageSrc = this.getItemImage(item);
            
            card.innerHTML = `
                <div class="h-48 bg-surface-container-high/50 relative overflow-hidden flex items-center justify-center border-b border-white/5">
                    <img class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" src="${imageSrc}" alt="${item.name}"/>
                    <div class="absolute top-4 right-4 bg-background/80 backdrop-blur-md px-2.5 py-1 rounded-lg border border-white/10 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px] text-[#FACC15]">star</span>
                        <span class="text-xs font-label-mono font-bold text-white">${item.rating}</span>
                    </div>
                </div>
                <div class="p-6 flex-1 flex flex-col">
                    <div class="flex justify-between items-start mb-2">
                        <span class="text-[10px] font-label-mono text-primary uppercase tracking-wider">${item.domain_category}</span>
                        <span class="text-sm font-label-mono text-secondary font-bold">$${item.price.toFixed(2)}</span>
                    </div>
                    <h3 class="text-base font-headline-md font-bold text-white mb-2 line-clamp-2 leading-snug">${item.name}</h3>
                    <p class="text-xs text-outline mb-4 line-clamp-3 flex-1 leading-relaxed">${item.description}</p>
                    
                    <div class="flex items-center justify-between pt-4 border-t border-white/5">
                        <div class="flex gap-2">
                            <span class="px-2 py-1 bg-surface-variant/50 rounded text-[9px] font-label-mono text-on-surface-variant">Score: ${(item.score).toFixed(1)}</span>
                        </div>
                        <div class="flex gap-2">
                            ${item.external_link ? `<a href="${item.external_link}" target="_blank" class="p-2 rounded-lg bg-primary/10 text-primary hover:bg-primary hover:text-on-primary transition-all" title="View Store"><span class="material-symbols-outlined text-sm">open_in_new</span></a>` : ''}
                            <button onclick="app.toggleLike(${item.id}, this)" class="p-2 rounded-lg bg-white/5 text-outline hover:text-secondary transition-all">
                                <span class="material-symbols-outlined text-sm">favorite_border</span>
                            </button>
                        </div>
                    </div>
                </div>
            `;
            grid.appendChild(card);
        });
    },

    async loadCollections() {
        const grid = document.getElementById('collections-grid');
        grid.innerHTML = '';
        try {
            const res = await fetch('/api/recommendations?q=&domain=All');
            const items = await res.json();
            
            // Render first 4 items in collections grid
            items.slice(0, 4).forEach(item => {
                const card = document.createElement('div');
                card.className = 'group glass-card p-0 rounded-3xl overflow-hidden flex flex-col solar-border';
                const imageSrc = this.getItemImage(item);
                
                card.innerHTML = `
                    <div class="relative aspect-square">
                        <img class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" src="${imageSrc}"/>
                        <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                            <span class="material-symbols-outlined text-primary text-4xl">visibility</span>
                        </div>
                    </div>
                    <div class="p-6">
                        <div class="font-headline-md text-base text-white mb-1 truncate">${item.name}</div>
                        <div class="font-label-mono text-[9px] text-secondary mb-4 uppercase">${item.domain_category}</div>
                        <div class="flex justify-between items-center">
                            <span class="font-label-mono text-sm font-bold text-white">$${item.price.toFixed(2)}</span>
                            <div class="flex gap-2">
                                ${item.external_link ? `<a href="${item.external_link}" target="_blank" class="p-2 rounded-lg bg-primary/10 text-primary hover:bg-primary hover:text-on-primary transition-colors"><span class="material-symbols-outlined text-sm">open_in_new</span></a>` : ''}
                                <button onclick="alert('Added to cart!')" class="p-2 rounded-lg bg-primary/10 text-primary hover:bg-primary hover:text-on-primary transition-colors">
                                    <span class="material-symbols-outlined text-sm">add_shopping_cart</span>
                                </button>
                            </div>
                        </div>
                    </div>
                `;
                grid.appendChild(card);
            });
        } catch (e) {
            console.error(e);
        }
    },

    async toggleLike(itemId, btn) {
        try {
            const icon = btn.querySelector('.material-symbols-outlined');
            const isLiked = icon.textContent === 'favorite';
            const action = isLiked ? 'unlike' : 'like';
            
            const body = new URLSearchParams();
            body.append('itemId', itemId);
            body.append('action', action);

            const res = await fetch('/api/interact', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            });

            if (res.ok) {
                if (isLiked) {
                    btn.classList.remove('text-secondary');
                    btn.classList.add('text-outline');
                    icon.textContent = 'favorite_border';
                } else {
                    btn.classList.remove('text-outline');
                    btn.classList.add('text-secondary');
                    icon.textContent = 'favorite';
                }
            }
        } catch (e) {
            console.error(e);
        }
    },

    async loadStats() {
        try {
            const res = await fetch('/api/stats');
            const stats = await res.json();
            document.getElementById('stat-users').textContent = stats.users;
            document.getElementById('stat-items').textContent = stats.items;
            document.getElementById('stat-searches').textContent = stats.searches;
        } catch (e) {
            console.error(e);
        }
    },

    async importCsv() {
        const path = document.getElementById('admin-csv-path').value;
        const statusEl = document.getElementById('import-status');
        if (!path) {
            alert('Please specify a CSV file path');
            return;
        }

        statusEl.className = "text-xs font-label-mono text-center mt-2 text-primary";
        statusEl.textContent = "Processing batch ingestion...";
        statusEl.classList.remove('hidden');

        try {
            const body = new URLSearchParams();
            body.append('csvPath', path);

            const res = await fetch('/api/import', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            });

            if (res.ok) {
                const data = await res.json();
                statusEl.className = "text-xs font-label-mono text-center mt-2 text-secondary";
                statusEl.textContent = `Success: Ingested ${data.imported} items (Skipped ${data.skipped})`;
                this.loadStats();
            } else {
                const data = await res.json();
                statusEl.className = "text-xs font-label-mono text-center mt-2 text-error";
                statusEl.textContent = data.message || "Failed to import CSV";
            }
        } catch (e) {
            statusEl.className = "text-xs font-label-mono text-center mt-2 text-error";
            statusEl.textContent = "System Error during ingestion execution";
        }
    },

    async sendAiMessage() {
        const input = document.getElementById('ai-chat-input');
        const text = input.value.trim();
        if (!text) return;

        input.value = '';
        const chatLog = document.getElementById('ai-chat-log');

        // Append User bubble
        const userDiv = document.createElement('div');
        userDiv.className = 'flex gap-4 items-start flex-row-reverse';
        userDiv.innerHTML = `
            <div class="w-8 h-8 rounded-full bg-secondary/20 flex items-center justify-center border border-secondary/20">
                <span class="material-symbols-outlined text-secondary text-lg">person</span>
            </div>
            <div class="max-w-[80%] text-right">
                <p class="font-body-md text-sm text-on-surface bg-primary/10 p-3.5 rounded-2xl rounded-tr-none text-left leading-relaxed">
                    ${text}
                </p>
                <span class="font-label-mono text-[9px] text-on-surface-variant mt-1 block">YOU · JUST NOW</span>
            </div>
        `;
        chatLog.appendChild(userDiv);
        chatLog.scrollTop = chatLog.scrollHeight;

        // Fetch matches from engine
        try {
            const res = await fetch(`/api/recommendations?q=${encodeURIComponent(text)}&domain=All`);
            const items = await res.json();

            // Append Assistant response bubble
            const assistDiv = document.createElement('div');
            assistDiv.className = 'flex gap-4 items-start';
            
            let itemsHtml = '';
            if (items.length > 0) {
                itemsHtml = `
                    <div class="mt-4 flex gap-4 overflow-x-auto pb-2 glass-scroll w-full">
                        ${items.slice(0, 3).map(item => `
                            <div class="glass-card p-3 rounded-2xl w-48 shrink-0 flex flex-col border border-white/5">
                                <img src="${this.getItemImage(item)}" class="h-24 w-full object-cover rounded-xl mb-2" />
                                <div class="font-headline-md text-xs text-white truncate font-bold">${item.name}</div>
                                <div class="flex justify-between items-center mt-2">
                                    <span class="font-label-mono text-xs text-secondary font-bold">$${item.price.toFixed(2)}</span>
                                    <button onclick="alert('Added to shopping list!')" class="p-1 rounded bg-primary/20 text-primary hover:bg-primary hover:text-on-primary transition-colors">
                                        <span class="material-symbols-outlined text-xs">add</span>
                                    </button>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }

            assistDiv.innerHTML = `
                <div class="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center border border-primary/20">
                    <span class="material-symbols-outlined text-primary text-lg">psychology</span>
                </div>
                <div class="max-w-[80%] w-full">
                    <div class="font-body-md text-sm text-on-surface bg-white/5 p-3.5 rounded-2xl rounded-tl-none leading-relaxed">
                        ${items.length > 0 
                            ? `I found these catalog entries in the database matching your request for "${text}":`
                            : `I couldn't find any exact matches for "${text}" in our database catalog. Try using general categories (e.g. food, tech, shoes).`}
                        ${itemsHtml}
                    </div>
                    <span class="font-label-mono text-[9px] text-on-surface-variant mt-1 block">ASSISTANT · JUST NOW</span>
                </div>
            `;
            chatLog.appendChild(assistDiv);
            chatLog.scrollTop = chatLog.scrollHeight;
        } catch (e) {
            console.error(e);
        }
    },

    initThreeJs() {
        if (this.threeJsScene) return; // Prevent double rendering
        
        const container = document.getElementById('threejs-container-ANIMATION');
        if (!container) return;

        const w = container.clientWidth || 800;
        const h = container.clientHeight || 320;

        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(50, w / h, 0.1, 1000);
        const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
        renderer.setSize(w, h);
        container.appendChild(renderer.domElement);

        // Core holographic outer wireframe
        const geometry = new THREE.SphereGeometry(1.6, 32, 32);
        const material = new THREE.MeshBasicMaterial({
            color: 0x38BDF8, // Cyber Blue
            wireframe: true,
            transparent: true,
            opacity: 0.4
        });
        const orb = new THREE.Mesh(geometry, material);
        scene.add(orb);

        // Inner solid core reflecting lime lighting
        const coreGeo = new THREE.IcosahedronGeometry(0.8, 1);
        const coreMat = new THREE.MeshStandardMaterial({
            color: 0xBEF264, // Electric Lime
            metalness: 0.8,
            roughness: 0.2
        });
        const core = new THREE.Mesh(coreGeo, coreMat);
        scene.add(core);

        // Light rays
        const light = new THREE.DirectionalLight(0xffffff, 1.5);
        light.position.set(2, 5, 2);
        scene.add(light);
        
        const ambLight = new THREE.AmbientLight(0x051424, 1.0);
        scene.add(ambLight);

        camera.position.z = 4.5;
        this.threeJsScene = scene;

        function renderFrame() {
            requestAnimationFrame(renderFrame);
            orb.rotation.y += 0.005;
            orb.rotation.x += 0.003;
            core.rotation.y -= 0.008;
            
            // Slight pulse animation
            const pulse = 1 + Math.sin(Date.now() * 0.0015) * 0.03;
            orb.scale.set(pulse, pulse, pulse);
            
            renderer.render(scene, camera);
        }
        renderFrame();

        window.addEventListener('resize', () => {
            const newW = container.clientWidth;
            const newH = container.clientHeight;
            renderer.setSize(newW, newH);
            camera.aspect = newW / newH;
            camera.updateProjectionMatrix();
        });
    }
};

window.onload = () => app.init();
window.app = app;
