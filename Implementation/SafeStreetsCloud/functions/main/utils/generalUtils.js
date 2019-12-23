exports.getNewDateWithAddedHours = function(date, hours) {
    return new Date(date.getTime() + (hours * 60 * 60 * 1000));
};