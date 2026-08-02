# tpm-auth-service

Issues signed JWT tokens for the TPM system.

Part of a set of four repositories — **start with
[tpm-platform](https://github.com/jklejczyk/tpm-platform)**, which explains the architecture and runs
everything together.

## This is a stand-in, on purpose

It is deliberately minimal: users in a database seeded by migration, BCrypt password hashes,
one endpoint. No refresh tokens, no registration, no password reset, no logout.

In production this role belongs to Keycloak, Auth0 or an equivalent — one does not write one's
own identity provider. It exists here so that the demo runs from a clean clone without an
account in an external service.

The part that is meant to be judged is the **other** side of the problem: the domain services
verifying signatures as resource servers. Those do not know who issues their tokens, so pointing
them at a real provider is a change of one configuration property.

## Endpoint

```bash
curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"kierownik","password":"kierownik"}'
```

```json
{ "token": "eyJhbGciOiJSUzI1NiJ9...", "expiresIn": 1800 }
```

Wrong credentials return `401` without revealing which half was wrong — distinguishing them
would turn the endpoint into a user enumeration oracle.

The token carries `sub` (the user id) and a `role` claim, and is signed with RS256.

## Trying it by hand

```bash
# Health - open
curl -s localhost:8080/actuator/health

# Each of the three demo users
for u in operator technik kierownik; do
  echo -n "$u -> "
  curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
    -d "{\"username\":\"$u\",\"password\":\"$u\"}"
  echo
done

# Wrong password -> 401
curl -s -i -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"technik","password":"wrong"}' | head -1

# Unknown user -> 401, identical response, on purpose
curl -s -i -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"nobody","password":"nobody"}' | head -1

# Missing field -> 400
curl -s -i -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"technik"}' | head -1
```

### Reading a token

A JWT is signed, not encrypted — the payload is plain base64 and anybody can read it. What
nobody can do without the private key is produce a matching signature for altered content.

```bash
TOKEN=$(curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"technik","password":"technik"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null; echo
# {"iss":"tpm-auth-service","sub":"tech-1","role":"TECHNICIAN","exp":...,"iat":...}
```

Tamper with a token and the resource servers reject it:

```bash
curl -s -i localhost:8081/machines/anything \
  -H "Authorization: Bearer ${TOKEN}tampered" | head -1
# 401
```

## Keys

This is the only service holding the **private** key; every other one gets the public half and
can therefore verify tokens but never mint them. The private key is mounted read-only at
runtime and is deliberately absent from both the repository and the image — it belongs to the
identity provider, not to the delivered system.

## Demo users

Seeded by `V2__demo_users.sql`. Passwords equal usernames.

| Username | Role |
|---|---|
| `operator` | `OPERATOR` |
| `technik` | `TECHNICIAN` |
| `kierownik` | `MANAGER` |

Hashes rather than plaintext even in demo fixtures, because there is no reason to do it the
wrong way and it costs one line.

## Running it on its own

```bash
docker compose up --build -d
```

Requires the key pair to exist in `../tpm-platform/keys/` — see the platform README.
