package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtService {

    fun generateToken(
        id: Int,
        correo: String
    ): String {

        return JWT.create()

            .withAudience(AuthConfig.audience)

            .withIssuer(AuthConfig.issuer)

            .withClaim("id", id)

            .withClaim("correo", correo)

            .withExpiresAt(
                Date(System.currentTimeMillis() + 86400000)
            )

            .sign(
                Algorithm.HMAC256(AuthConfig.secret)
            )

    }

}