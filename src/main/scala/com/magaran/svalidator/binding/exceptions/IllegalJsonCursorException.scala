package com.magaran.svalidator.binding.exceptions

import io.circe.ParsingFailure

/** Thrown when attempting to use a JsonCursor instance obtained from parsing an invalid json string.
  *
  * @param receivedJsonString The json string that was received
  * @param cause              The cause of the exception
  */
final class IllegalJsonCursorException(val receivedJsonString: String, val cause: ParsingFailure)
    extends Exception(
      "Attempted to use a JsonCursor instance obtained from parsing an invalid json string.\n " +
        "You may inspect the cause of this exception to see where the cursor was obtained from.\n" +
        s"The received json string was $receivedJsonString",
      cause
    )
