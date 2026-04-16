package com.magaran.svalidator.config

/** Specifies the style how to manage edge whitespace when binding strings using the default string binder. */
enum StringBindingStyle:
  /** String binding style that fails binding if the string is empty or contains only whitespace.   All whitespace is
    * preserved and the binding is successful if the string is not completely whitespace
    */
  case InvalidateFullWhitespacePreserveWhitespaces

  /** String binding style that trims all whitespace from the string before binding.  The binding is successful if the
    * resulting trimmed string is not empty.
    */
  case TrimWhitespaceInvalidateEmpty
