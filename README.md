# NHS Jobs Gateway

Provider-specific adapter for the official NHS Jobs Self-Serve Job Adverts API.
It maps NHS XML vacancy results to a stable Job Seeker Copilot gateway contract
and keeps provider policy, attribution, links and failures at one boundary.

Status: **first technical checkpoint; not approved for invited or public beta**.
Written NHSBSA confirmation of commercial display, caching,
matching/deduplication and linking is a release gate.

## Modes

`EXTERNAL_PROVIDER_MODE=FIXTURE` is the safe default. It serves a deterministic,
fully synthetic dataset from this process, uses only reserved `.test` links and
makes no external request. It cannot run with a `prod` or `production` profile.

`EXTERNAL_PROVIDER_MODE=LIVE` calls only the documented public
`https://www.jobs.nhs.uk/api/v1/search_xml` endpoint. The public contract does
not expose employer codes or internal/external switches, so callers cannot ask
this gateway for employer-scoped or internal vacancies. `NHS_JOBS_ENABLED=false`
is the provider kill switch.

## Local verification

```bash
mvn -B --no-transfer-progress clean verify
docker build --tag local/nhs-jobs-gateway .
```

Run locally in fixture mode:

```bash
mvn spring-boot:run
curl -s http://localhost:8104/internal/provider-mode
curl -s -X POST http://localhost:8104/api/v1/nhs/jobs/search \
  -H 'Content-Type: application/json' \
  -d '{"keyword":"nurse","page":1,"resultsPerPage":20}'
```

The official contract and usage constraints captured for this checkpoint are
in [`docs/NHS_JOBS_CONTRACT.md`](docs/NHS_JOBS_CONTRACT.md).
