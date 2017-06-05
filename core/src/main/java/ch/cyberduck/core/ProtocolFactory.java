package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ProtocolFactory {
    private static final Logger log = Logger.getLogger(ProtocolFactory.class);

    public static final ProtocolFactory global = new ProtocolFactory();

    private final Set<Protocol> registered;
    private final Local bundle;

    public ProtocolFactory() {
        this(new LinkedHashSet<Protocol>());
    }

    public ProtocolFactory(final Set<Protocol> protocols) {
        this(LocalFactory.get(PreferencesFactory.get().getProperty("application.profiles.path")), protocols);
    }

    public ProtocolFactory(final Local bundle, final Set<Protocol> protocols) {
        this.bundle = bundle;
        this.registered = protocols;
    }

    public void register(Protocol... protocols) {
        // Order determines list in connection dropdown
        for(Protocol protocol : protocols) {
            register(protocol);
        }
        if(bundle.exists()) {
            try {
                for(Local f : bundle.list().filter(new ProfileFilter())) {
                    final Profile profile = ProfileReaderFactory.get().read(f);
                    if(null == profile.getProtocol()) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding bundled protocol %s", profile));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    registered.add(profile);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", bundle, e.getMessage()));
            }
        }
        // Load thirdparty protocols
        final Local library = LocalFactory.get(PreferencesFactory.get().getProperty("application.support.path"),
                PreferencesFactory.get().getProperty("profiles.folder.name"));
        if(library.exists()) {
            try {
                for(Local profile : library.list().filter(new ProfileFilter())) {
                    final Profile protocol = ProfileReaderFactory.get().read(profile);
                    if(null == protocol) {
                        continue;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Adding thirdparty protocol %s", protocol));
                    }
                    // Replace previous possibly disable protocol in Preferences
                    registered.add(protocol);
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure reading collection %s %s", library, e.getMessage()));
            }
        }
    }

    public void register(final Protocol protocol) {
        registered.add(protocol);
    }

    /**
     * @return List of protocols
     */
    public List<Protocol> find() {
        return this.find(Protocol::isEnabled);
    }

    public List<Protocol> find(final Predicate<Protocol> search) {
        return registered.stream().filter(search).sorted().collect(Collectors.toList());
    }

    /**
     * @param identifier Provider name or hash code of protocol
     * @return Matching protocol or null if no match
     */
    public Protocol find(final String identifier) {
        return this.forName(this.find(), identifier);
    }

    public Protocol forName(final List<Protocol> protocols, final String identifier) {
        for(Protocol protocol : protocols) {
            if(protocol.getProvider().equals(identifier)) {
                return protocol;
            }
        }
        for(Protocol protocol : protocols) {
            if(String.valueOf(protocol.hashCode()).equals(identifier)) {
                return protocol;
            }
        }
        return this.forScheme(identifier);
    }

    /**
     * @param scheme Protocol scheme
     * @return Standard protocol for this scheme. This is ambigous
     */
    public Protocol forScheme(final Scheme scheme) {
        return forScheme(scheme.name());
    }

    public Protocol forType(final Protocol.Type type) {
        for(Protocol protocol : registered) {
            if(protocol.getType().equals(type)) {
                return protocol;
            }
        }
        log.warn(String.format("Unknown type %s", type));
        return null;
    }

    public Protocol forScheme(final String scheme) {
        return this.forScheme(registered, scheme);
    }

    public Protocol forScheme(final Set<Protocol> protocols, final String scheme) {
        for(Protocol protocol : protocols) {
            for(String s : protocol.getSchemes()) {
                if(s.equals(scheme)) {
                    return protocol;
                }
            }
        }
        log.warn(String.format("Unknown scheme %s", scheme));
        return null;
    }

    private static final class ProfileFilter implements Filter<Local> {
        @Override
        public boolean accept(final Local file) {
            return "cyberduckprofile".equals(FilenameUtils.getExtension(file.getName()));
        }

        @Override
        public Pattern toPattern() {
            return Pattern.compile(".*\\.cyberduckprofile");
        }
    }
}