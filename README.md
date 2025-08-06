# Movie Discovery App

This repository contains a Movie Discovery App that allows users to explore a list of movies, view detailed information, and simulate booking a movie. The app fetches movie data from The Movie Database (TMDb) API.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technical Details](#technical-details)
- [Setup and Installation](#setup-and-installation)
- [Screenshots](#screenshots)
- [Unit Testing](#unit-testing)
- [License](#license)

## Overview

The Movie Discovery App is designed to help users easily discover movies, view details, and simulate movie bookings. The app fetches data from The Movie Database (TMDb) API and displays a list of available movies, sorted by release date, popularity, or rating.

## Features

- **Home Screen**:
  - A list of available movies, sorted by release date (default), alphabetical order, or rating.
  - Pull-to-refresh functionality to reload the movie list.
  - Infinite scroll to load more movies when reaching the bottom.
  - Each movie displays:
    - Poster/Backdrop image
    - Title
    - Popularity
  
- **Detail Screen**:
  - Upon selecting a movie, users can view:
    - Synopsis
    - Genres
    - Language
    - Duration
    - A simulated "Book" option that opens the movie booking link in a WebView.

## Technical Details

### Platform Options

This app is designed for the following platforms:

- **iOS**: Built using Swift (minimum version 4.0) with RxSwift and MVVM architecture.
- **Android**: Developed with Kotlin, RxJava, and MVVM architecture.
- **Web**: Built with ReactJS or Vanilla JS using CSS/SASS for styling.

### Libraries and Tools

- **RxSwift** or **RxKotlin** for reactive programming.
- **MVVM** or **MVP** architecture for separation of concerns.
- **Unit Testing**: All critical functionalities are covered by unit tests.
- **Dependency Injection**: Managed using tools like Dagger, Koin, or Hilt.
- **State Management**: Handled through the appropriate methods based on the platform (e.g., Context API for React, View Binding for Android).
