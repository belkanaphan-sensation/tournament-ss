package org.bn.jj.service;

import org.bn.jj.dto.auth.AuthenticationRequest;
import org.bn.jj.dto.auth.AuthenticationResponse;
import org.bn.jj.dto.auth.RegisterRequest;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    AuthenticationResponse register(RegisterRequest registerRequest);
}
