/**
 * This function allow you to modify a JS Promise by adding some status properties.
 * Based on: http://stackoverflow.com/questions/21485545/is-there-a-way-to-tell-if-an-es6-promise-is-fulfilled-rejected-resolved
 * But modified according to the specs of promises : https://promisesaplus.com/
 */
exports.makeQueryablePromise = function(promise) {
    // Don't modify any promise that has been already modified.
    if (promise.isResolved) return promise;

    // Set initial state
    let isPending = true;
    let isRejected = false;
    let isFulfilled = false;
    let response = null;

    // Observe the promise, saving the fulfillment in a closure scope.
    let result = promise.then(
        v => {
            isFulfilled = true;
            isPending = false;
            response = v[0];
            return v;
        },
        e => {
            isRejected = true;
            isPending = false;
            throw e;
        }
    );

    result.isFulfilled = function() { return isFulfilled; };
    result.isPending = function() { return isPending; };
    result.isRejected = function() { return isRejected; };
    result.getResponse = function() { return response; };
    return result;
};
