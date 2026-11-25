# Known API issues

## `POST /booking` returns 500 when required fields are missing

**Status:** Reproduced against the public Restful Booker API on 2026-11-25.

The negative test `BookingNegativeTest.postWithMissingRequiredFieldsIsRejected()` sends this JSON body:

```json
{"firstname":"Incomplete"}
```

The request is sent as `application/json` to `POST https://restful-booker.herokuapp.com/booking`. The booking creation client serializes the supplied map directly; the payload is valid JSON and deliberately omits required booking fields. The test expects HTTP 400 for this invalid client request. The public API currently responds with HTTP 500 instead. A direct request with the same body reproduced HTTP 500, as did the Maven/Surefire run.

This is an upstream input-validation defect: the service returns a server-error status for an incomplete request. The test expectation remains HTTP 400 so that the contract violation stays visible. Do not change the expected status to 500 or suppress the test to obtain a green build.

### CI handling

The current test suite targets a shared public demo service, so its results depend on an external service and network conditions. For a reliable blocking CI gate, run the unchanged contract suite against a controlled Restful Booker-compatible test deployment where this validation is fixed. Run the public demo API checks as a separate, clearly reported upstream integration check; keep its failure visible and retain the Surefire reports. Until a controlled target is available or the public service is fixed, a failing build on this test is an accurate report of the known API defect, not a test implementation failure.

## Response-time assertion

The suite currently requires each measured request to complete in less than 2000 ms. The reported 2515 ms result followed by 1385 ms on a second execution indicates timing variability consistent with a shared public service or network path. The threshold was left unchanged: there is not enough repeated measurement data here to justify changing the SLA or its coverage. CI reports should preserve and publish measured durations; if response-time performance is a release gate, measure it separately against a controlled target over repeated samples rather than interpreting a single public-demo request as a stable performance result.
