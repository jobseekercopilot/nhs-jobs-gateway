# NHS Jobs Gateway

NHS Jobs Gateway is the Job Seeker Copilot boundary around the official NHS
Jobs Self-Serve Job Adverts XML feed. It exposes a small internal JSON API and
preserves NHS vacancy identity, employer, dates, salary, locations, and the
official listing URL without scraping candidate pages.

The authoritative provider endpoint is `https://www.jobs.nhs.uk/api/v1/search_xml`.
The retired `/api/v1/vacancies` endpoint is intentionally not used.

NHS Jobs Service Delivery confirmed in writing on 31 July 2026 that the
Job Seeker Copilot use described for this endpoint is acceptable. The approved
use covers minimised public-vacancy display, TTL-bounded caching, prompt expiry
removal, cross-provider matching/deduplication, clear NHS Jobs attribution and
official redirects in invited beta and a later commercial product. The service
does not ingest applicant data or internal-only vacancies, scrape candidate
pages, or imply NHS endorsement.

## Modes and configuration

`EXTERNAL_PROVIDER_MODE=FIXTURE` is the safe local/E2E default and makes no
external request. A production profile rejects fixture mode. Set
`EXTERNAL_PROVIDER_MODE=LIVE` to call the official feed; NHS does not require a
credential for this interface. `NHS_JOBS_ENABLED=false` is the kill switch.

| Variable | Default | Purpose |
| --- | --- | --- |
| `NHS_JOBS_BASE_URL` | official `search_xml` URL | Provider endpoint |
| `NHS_JOBS_ENABLED` | `true` | Live-provider kill switch |
| `NHS_JOBS_RESULTS_PER_PAGE` | `50` | Page size, clamped to 1–100 |
| `NHS_JOBS_DEFAULT_COUNTRY_CODE` | `GB-ENG` | Default location filter |

## Local verification

```bash
mvn -B clean verify
docker build -t local/nhs-jobs-gateway .
```

The internal contract is published at `api/openapi.yaml`; runtime mode is
observable at `GET /internal/provider-mode` and health at `GET /actuator/health`.
