// Global variables to store logged-in user info
// Retrieve user info from sessionStorage immediately when the script is parsed
let loggedInUserId = sessionStorage.getItem('loggedInUserId');
let loggedInUserType = sessionStorage.getItem('loggedInUserType');
let loggedInUserName = sessionStorage.getItem('loggedInUserName'); // Certifique-se de que este também é global

// Make these global so other scripts can access them immediately
window.loggedInUserId = loggedInUserId;
window.loggedInUserType = loggedInUserType;
window.loggedInUserName = loggedInUserName; // Torna o nome também global

document.addEventListener('DOMContentLoaded', () => {
    // Function to load the header HTML
    async function loadHeader() {
        console.log("header.js: loadHeader: Starting header loading process.");
        try {
            const response = await fetch('header.html'); // Fetch the header HTML file
            if (!response.ok) {
                console.error(`header.js: loadHeader: HTTP error! status: ${response.status} for header.html`);
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const headerHtml = await response.text();
            console.log("header.js: loadHeader: header.html fetched successfully.");

            // Create a temporary div to parse the fetched HTML
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = headerHtml;
            console.log("header.js: loadHeader: header.html parsed into tempDiv.");

            // Extract the <style> and <header> elements
            const styleElement = tempDiv.querySelector('style');
            const headerElement = tempDiv.querySelector('header');
            console.log("header.js: loadHeader: styleElement found:", !!styleElement);
            console.log("header.js: loadHeader: headerElement found:", !!headerElement);

            // Find the target element where the header should be inserted
            // CORREÇÃO AQUI: Usar getElementById para corresponder ao HTML
            const headerPlaceholder = document.getElementById('header-placeholder'); 
            console.log("header.js: loadHeader: headerPlaceholder found (by ID):", !!headerPlaceholder);

            if (headerPlaceholder) {
                // Insert the style into the head if it's not already there
                if (styleElement && !document.head.querySelector(`style[data-header-style]`)) {
                    styleElement.setAttribute('data-header-style', 'true'); // Add a marker to prevent duplicates
                    document.head.appendChild(styleElement);
                    console.log("header.js: loadHeader: Style appended to head.");
                }
                // Replace the placeholder with the loaded header content
                headerPlaceholder.replaceWith(headerElement);
                console.log("header.js: loadHeader: Header content replaced placeholder.");
            } else {
                console.warn("header.js: No element with ID 'header-placeholder' found. Prepending header to body."); // Mensagem de aviso atualizada
                if (styleElement && !document.head.querySelector(`style[data-header-style]`)) {
                    styleElement.setAttribute('data-header-style', 'true');
                    document.head.appendChild(styleElement);
                    console.log("header.js: loadHeader: Style appended to head (fallback).");
                }
                document.body.prepend(headerElement);
                console.log("header.js: loadHeader: Header content prepended to body (fallback).");
            }

            // After the header is in the DOM, initialize its interactive elements
            initializeHeaderSearchBar();
            initializeProfileBehavior(); // Handle conditional profile behavior

            // Update all dynamic content (browser tab title) after the header is loaded
            updateHeaderContent();
            console.log("header.js: updateHeaderContent called.");

        } catch (error) {
            console.error('header.js: Failed to load header:', error);
        }
    }

    // Function to update dynamic content based on body data attributes
    function updateHeaderContent() {
        console.log("header.js: updateHeaderContent: Starting content update.");

        const body = document.body;
        const pageTitle = body.dataset.title || 'Default Page Title';
        const showSearchBarData = body.dataset.showSearchBar === 'true';

        console.log("header.js: updateHeaderContent: Detected pageTitle from body data-attribute:", pageTitle);

        // 1. Update the browser tab title
        document.title = pageTitle;
        console.log("header.js: updateHeaderContent: Browser tab title updated to:", pageTitle);

        // 2. Control visibility of the header search bar
        const headerSearchBar = document.querySelector('.header-search-bar');
        if (headerSearchBar) {
            headerSearchBar.style.display = showSearchBarData ? 'flex' : 'none';
            console.log("header.js: updateHeaderContent: Header search bar visibility set to:", showSearchBarData);
        } else {
            console.warn("header.js: updateHeaderContent: Could not find element with class 'header-search-bar'.");
        }

        console.log("header.js: updateHeaderContent: Content update finished.");
    }

    // Function to attach event listeners to the search bar elements
    function initializeHeaderSearchBar() {
        const headerTagSearchInput = document.getElementById('headerTagSearchInput');
        const headerSearchIcon = document.getElementById('headerSearchIcon');

        if (headerTagSearchInput && headerSearchIcon) {
            console.log("header.js: Initializing header search bar event listeners.");
            
            // Listen for Enter key press on the input field
            headerTagSearchInput.addEventListener('keypress', (event) => {
                if (event.key === 'Enter') {
                    console.log("header.js: Enter key pressed. Input value:", headerTagSearchInput.value);
                    dispatchSearchEvent(headerTagSearchInput.value);
                }
            });

            // Listen for click on the search icon
            headerSearchIcon.addEventListener('click', () => {
                console.log("header.js: Search icon clicked. Input value:", headerTagSearchInput.value);
                dispatchSearchEvent(headerTagSearchInput.value);
            });

        } else {
            console.warn("header.js: Header search input or icon not found, search functionality not initialized.");
        }
    }

    // Handle conditional profile icon behavior (dropdown vs redirect)
    function initializeProfileBehavior() {
        const profileIconContainer = document.getElementById('profile-icon-container');
        const profileDropdown = document.getElementById('profileDropdown');
        const configLink = document.getElementById('configLink');
        const logoutButton = document.getElementById('logoutButton');

        // NOVO: Obter referências aos links de carrinho e pedidos
        const cartLink = document.getElementById('cartLink');
        const consumersOrdersLink = document.getElementById('consumersOrdersLink');

        if (!profileIconContainer) {
            console.warn("header.js: Profile icon container not found. Cannot initialize profile behavior.");
            return;
        }

        // Use the global loggedInUserId e loggedInUserType
        if (window.loggedInUserId) { // User is logged in: Show dropdown on click
            console.log("header.js: User is logged in. Initializing profile dropdown behavior.");
            
            // Set config link
            if (configLink) {
                configLink.href = `/updateProfile.html?userId=${window.loggedInUserId}`;
            }

            // Ensure dropdown is hidden initially
            if (profileDropdown) {
                profileDropdown.classList.add('hidden');
            }

            // --- INÍCIO DA LÓGICA PARA VISIBILIDADE CONDICIONAL DOS LINKS ---
            if (window.loggedInUserType === 'consumer') {
                if (cartLink) cartLink.classList.remove('hidden'); // Mostrar carrinho
                if (consumersOrdersLink) consumersOrdersLink.classList.remove('hidden'); // Mostrar pedidos
            } else {
                // Para vendedores ou outros tipos, ocultar esses links
                if (cartLink) cartLink.classList.add('hidden'); // Ocultar carrinho
                if (consumersOrdersLink) consumersOrdersLink.classList.add('hidden'); // Ocultar pedidos
            }
            // --- FIM DA LÓGICA PARA VISIBILIDADE CONDICIONAL DOS LINKS ---

            // Attach click listener to toggle dropdown
            profileIconContainer.addEventListener('click', (event) => {
                event.stopPropagation(); // Prevent click from immediately closing the dropdown
                if (profileDropdown) {
                    profileDropdown.classList.toggle('hidden');
                    console.log("header.js: Profile icon clicked. Dropdown toggled.");
                }
            });

            // Close dropdown if clicked outside
            document.addEventListener('click', (event) => {
                if (profileDropdown && !profileDropdown.contains(event.target) && !profileIconContainer.contains(event.target)) {
                    profileDropdown.classList.add('hidden');
                    console.log("header.js: Clicked outside. Dropdown hidden.");
                }
            });

            // Add event listener for logout button
            if (logoutButton) {
                logoutButton.addEventListener('click', () => {
                    sessionStorage.removeItem('loggedInUserId');
                    sessionStorage.removeItem('loggedInUserName');
                    sessionStorage.removeItem('loggedInUserType');
                    sessionStorage.removeItem('cartItems'); // Clear cart on logout
                    console.log("header.js: User logged out. Session cleared.");
                    window.location.href = '/login.html'; // Redirect to login page
                });
            }

        } else { // User is NOT logged in: Redirect to login page on click
            console.log("header.js: User is NOT logged in. Initializing profile icon redirect behavior.");
            // Hide the dropdown menu
            if (profileDropdown) {
                profileDropdown.classList.add('hidden');
            }
            // Também ocultar links de carrinho e pedidos se não estiver logado
            if (cartLink) cartLink.classList.add('hidden');
            if (consumersOrdersLink) consumersOrdersLink.classList.add('hidden');

            // Remove any existing click listeners to avoid conflicts (important for re-renders)
            // Clone the element to remove all previous listeners, then re-add to DOM
            const oldProfileIconContainer = profileIconContainer;
            const newProfileIconContainer = oldProfileIconContainer.cloneNode(true);
            oldProfileIconContainer.parentNode.replaceChild(newProfileIconContainer, oldProfileIconContainer);

            // Attach click listener to redirect to login on the new element
            newProfileIconContainer.addEventListener('click', () => {
                console.log("header.js: Profile icon clicked! Redirecting to login.html");
                window.location.href = '/login.html';
            });
        }
    }

    // Dispatches a custom event with the search query to other parts of the application
    function dispatchSearchEvent(searchTags) {
        console.log("header.js: Dispatching 'productSearch' event with tags:", searchTags);
        const event = new CustomEvent('productSearch', {
            detail: { searchTags: searchTags }
        });
        window.dispatchEvent(event); // Dispatch the event on the window object
    }

    // Load the header when the DOM is ready
    loadHeader();
});
