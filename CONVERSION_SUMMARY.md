# Block Subtitle Chrome Extension

This project has successfully converted the Android "Block Subtitle" app into a Chrome extension that creates a resizable floating overlay to block subtitles or other UI elements on web pages.

## Files Created

### Core Extension Files
- `manifest.json` - Extension configuration and metadata
- `popup.html` - User interface for the extension popup
- `popup.js` - Logic for the popup interface
- `content.js` - Main functionality for the overlay (dragging, resizing, visibility)
- `README.md` - General documentation for the extension
- `ICON_CONVERSION.md` - Instructions for converting SVG icons to PNG
- `test.html` - Testing instructions and page

### Icon Files (SVG format)
- `icon128.svg` - 128x128 pixel icon
- `icon48.svg` - 48x48 pixel icon
- `icon16.svg` - 16x16 pixel icon

## Functionality Implemented

The Chrome extension replicates the key features of the Android app:

1. **Floating Overlay**: Creates a semi-transparent overlay that floats above web content
2. **Draggable**: Move the overlay by dragging the center area
3. **Resizable**: Resize the overlay by dragging any edge or corner
4. **Double-click to Hide**: Double-click the overlay to hide it
5. **Position & Size Memory**: Remembers the overlay position and size between sessions
6. **Popup Controls**: Toggle visibility and reset position from the extension popup

## How to Use

### Loading the Extension in Chrome
1. Convert the SVG icons to PNG format using the instructions in `ICON_CONVERSION.md`
2. Open Chrome and navigate to `chrome://extensions/`
3. Enable "Developer mode" in the top right corner
4. Click "Load unpacked" button
5. Select the extension folder in this project
6. The extension icon should appear in your Chrome toolbar

### Using the Extension
1. Click the extension icon in your toolbar
2. In the popup, click "Show Overlay" to create the blocking overlay
3. Drag the center area to move the overlay
4. Drag any edge or corner to resize the overlay
5. Double-click the overlay to hide it
6. Use the popup to toggle visibility or reset position

## Technical Details

### Architecture
- **manifest.json**: Defines extension permissions, content scripts, and UI
- **content.js**: Injected into web pages to create and manage the overlay
- **popup.html/popup.js**: Provides the extension's user interface

### Key Features
- Uses Chrome's storage API to persist overlay position and size
- Implements mouse and touch event handlers for dragging and resizing
- Creates resize handles on all sides and corners for intuitive resizing
- Handles window resizing to keep overlay within bounds

### Permissions
- `storage`: Required to save overlay position and size between sessions

## Differences from Android App

While the core functionality is preserved, there are some differences due to platform constraints:

1. Chrome extensions can't run as background services like Android services
   - The overlay only exists on active tabs where the content script is loaded
2. The overlay is specific to each tab rather than system-wide
3. Chrome extension uses web technologies (HTML/CSS/JS) rather than native Android views

## Future Enhancements

- Add color and transparency options for the overlay
- Implement multiple overlay support
- Add a quick toggle option from the browser action
- Improve edge detection for resizing