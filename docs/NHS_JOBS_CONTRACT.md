# NHS Jobs Self-Serve API contract

## Pinned source

- Document: **NHS Jobs Self-Serve Job Adverts API v1.07**
- Published: 1 May 2026
- Official source: <https://www.nhsbsa.nhs.uk/sites/default/files/2026-05/NHS%20Jobs%20Self-Serve%20Job%20Adverts%20API%20V1.07_0.docx>
- SHA-256: `85f256c7662bfad3291e5b8b9adc3309ebac9cbdfb8dc9fbc726a7e9db022d09`

The upstream document is linked rather than redistributed. Re-check its
version and checksum before changing the client contract.

## Endpoint used

`GET https://www.jobs.nhs.uk/api/v1/search_xml`

An unscoped request returns public UK vacancies. This gateway intentionally
does not expose `employerCode`, `internalOnly` or `externalOnly`: internal-only
adverts must never be made public.

Supported checkpoint filters are `keyword`, `location`, `distance`,
`countryCode`, `contractType`, `staffGroup`, `workingPattern`, `payBand`,
`salaryFrom`, `salaryTo`, `publishedFrom`, `sort`, `page` and `limit`. The
provider maximum `limit` is 100.

## Response mapping

The `<nhsJobs>` wrapper supplies `totalPages` and `totalResults`. Each
`<vacancyDetails>` entry maps:

| NHS field | Gateway field |
| --- | --- |
| `id` | `externalJobId` |
| `reference` | `reference` |
| `title` | `title` |
| `employer` | `employer` |
| `description` | `description` |
| nested `locations` leaves | `locations` |
| `salary` | `salaryText` |
| `type` | `contractType` |
| `postDate` | `postedAt` |
| `closeDate` | `closesAt` |
| `url` | allowlisted `sourceUrl` and `applicationUrl` |

The description is already a truncated overview in the official response.
Dates and the provider salary string are preserved verbatim at this boundary.

## Usage and release gate

Development currently relies on the published
[NHS Jobs terms](https://www.jobs.nhs.uk/employer/terms-and-conditions),
[integration guidance](https://www.nhsbsa.nhs.uk/about-nhs-jobs/nhs-jobs-integration-and-benefits)
and Open Government Licence basis.

The product must provide attribution, link to the NHS Jobs source/application
page, minimise displayed data, remove expired vacancies promptly, avoid any
endorsement claim and use no longer than the permitted cache lifetime.

Written NHSBSA confirmation of commercial/invited-beta display, caching,
matching/deduplication and linking is mandatory before invited or public beta.
Until then this gateway is limited to deterministic fixture development and
isolated live-development contract validation.
