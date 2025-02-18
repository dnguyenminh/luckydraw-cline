# Technical Document: Error Handling & Notification Enhancements

## Overview

This document summarizes the changes made to enhance error handling and alert notifications in our application.

## Error Interceptor Improvements

- **Centralized Error Handling:**  
  The error interceptor now intercepts HTTP responses and maps different status codes to user-friendly error messages.

- **Type-Safe Error Messages:**  
  Error messages are stored in a constant file (`error-messages.constants.ts`) as a readonly mapping, ensuring type safety by using a union type for error keys.

- **Client vs. Server Errors:**  
  The interceptor distinguishes between client-side errors (handled via `ErrorEvent`) and server-side errors (handled via HTTP status codes). If a custom error message is returned from the server, it is shown directly.

- **Alert Integration:**  
  The interceptor leverages the alert service to display error notifications, using configurable options such as dismissibility, timeout, and icons.

- **Testing:**  
  A comprehensive unit test suite (`error.interceptor.spec.ts`) validates interceptor behavior for various HTTP error scenarios (statuses 0, 401, 500, etc.) and client errors.

## Alert & Notification Enhancements

- **Alert Types & Themes:**  
  The alert types have been expanded with clear definitions and theming to maintain visual consistency across the application. Themes include configurations for success, error, warning, and info alerts, with corresponding icons and color schemes.

- **Notification Constants:**  
  The notification constants in `notification.constants.ts` define default values, allowed types, positions, animation classes, and type guards. These constants are used throughout the application for alert styling and display.

## Integration & Usage

- **Error Handling Flow:**  
  1. An HTTP request is made.  
  2. The error interceptor catches any errors, maps them to a pre-defined error message based on the HTTP status code or custom message from the server.  
  3. The alert service is then used to display the error message with the proper configuration.

- **Testing:**  
  Developers can run the unit tests to verify that the error handling logic works as intended. This can be done using the standard Angular testing commands.

## Conclusion

The updates to the error interceptor and alert notification system improve the application's robustness and user experience by providing clear, consistent, and maintainable error feedback.

*End of Documentation.*