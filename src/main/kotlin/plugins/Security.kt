package com.example.plugins

import com.example.auth.AuthConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun Application.configureSecurity() {

    authentication {

        jwt("auth-jwt") {

            realm = AuthConfig.realm

            verifier(

                JWT
                    .require(
                        Algorithm.HMAC256(AuthConfig.secret)
                    )
                    .withAudience(AuthConfig.audience)
                    .withIssuer(AuthConfig.issuer)
                    .build()

            )

            validate { credential ->

                if (credential.payload.getClaim("correo").asString() != "")
                    JWTPrincipal(credential.payload)
                else
                    null

            }

        }

    }

}