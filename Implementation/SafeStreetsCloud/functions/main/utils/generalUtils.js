exports.getNewDateWithAddedHours = function(date, hours) {
    return new Date(date.getTime() + (hours * 60 * 60 * 1000));
};

exports.getNewDateWithDayPrecision = function(date) {
    return new Date(date.getUTCFullYear(), date.getMonth(), date.getDate(), 12);
};