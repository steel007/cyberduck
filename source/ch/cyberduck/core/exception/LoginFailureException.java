package ch.cyberduck.core.exception;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public class LoginFailureException extends BackgroundException {
    private static final long serialVersionUID = -7628228280711158915L;

    public LoginFailureException(final String detail) {
        super(detail, null);
    }

    public LoginFailureException(final String detail, final Exception cause) {
        super(detail, cause);
    }

    @Override
    public String getMessage() {
        return Locale.localizedString("Login failed", "Credentials");
    }
}
