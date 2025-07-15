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

            // Find the target element where the header should be inserted (using class now, as per new HTML)
            const headerPlaceholder = document.querySelector('.header-placeholder'); // Changed from getElementById to querySelector
            console.log("header.js: loadHeader: headerPlaceholder found (by class):", !!headerPlaceholder);

            if (headerPlaceholder) {
                // Insert the style into the head if it's not already there (and keep it there per user request)
                if (styleElement && !document.head.querySelector(`style[data-header-style]`)) {
                    styleElement.setAttribute('data-header-style', 'true'); // Add a marker to prevent duplicates
                    document.head.appendChild(styleElement);
                    console.log("header.js: loadHeader: Style appended to head.");
                }
                // Replace the placeholder with the loaded header content
                headerPlaceholder.replaceWith(headerElement); // Changed from appendChild to replaceWith
                console.log("header.js: loadHeader: Header content replaced placeholder.");
            } else {
                console.warn("header.js: No element with class 'header-placeholder' found. Prepending header to body.");
                if (styleElement && !document.head.querySelector(`style[data-header-style]`)) {
                    styleElement.setAttribute('data-header-style', 'true');
                    document.head.appendChild(styleElement);
                    console.log("header.js: loadHeader: Style appended to head (fallback).");
                }
                document.body.prepend(headerElement);
                console.log("header.js: loadHeader: Header content prepended to body (fallback).");
            }

            // After the header is in the DOM, initialize its interactive elements (like the search bar)
            initializeHeaderSearchBar(); // Call the new function for search bar setup

            // Update all dynamic content (browser tab title and element visibility) after the header is loaded
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
        const showProfileIcon = body.dataset.showProfileIcon === 'true';
        const showSearchBar = body.dataset.showSearchBar === 'true'; // Added search bar visibility

        console.log("header.js: updateHeaderContent: Detected pageTitle from body data-attribute:", pageTitle);

        // 1. Update the browser tab title
        document.title = pageTitle;
        console.log("header.js: updateHeaderContent: Browser tab title updated to:", pageTitle);

        // 2. Control visibility of the profile icon
        const profileIcon = document.querySelector('.profile-icon-container');
        if (profileIcon) {
            profileIcon.style.display = showProfileIcon ? 'flex' : 'none'; // Use 'flex' for display
            console.log("header.js: Profile icon visibility set to:", showProfileIcon);
            // Add event listener to the profile icon after it's loaded into the DOM
            if (showProfileIcon) {
                // Ensure listener is only added once to prevent multiple bindings
                if (!profileIcon.dataset.listenerAdded) { // Prevent adding multiple listeners
                    profileIcon.addEventListener('click', () => {
                        console.log("header.js: Profile icon clicked! Redirecting to login.html");
                        window.location.href = 'login.html';
                    });
                    profileIcon.dataset.listenerAdded = 'true'; // Mark listener as added
                }
            }
        } else {
            console.warn("header.js: Could not find element with class 'profile-icon-container'.");
        }

        // 3. Control visibility of the header search bar
        const headerSearchBar = document.querySelector('.header-search-bar');
        if (headerSearchBar) {
            headerSearchBar.style.display = showSearchBar ? 'flex' : 'none'; // Use 'flex' for display
            console.log("header.js: updateHeaderContent: Header search bar visibility set to:", showSearchBar);
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
