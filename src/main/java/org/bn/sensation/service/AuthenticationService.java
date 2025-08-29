package org.bn.sensation.service;

import org.bn.sensation.dto.auth.AuthenticationRequest;
import org.bn.sensation.dto.auth.AuthenticationResponse;
import org.bn.sensation.dto.auth.RegisterRequest;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    AuthenticationResponse register(RegisterRequest registerRequest);
}
