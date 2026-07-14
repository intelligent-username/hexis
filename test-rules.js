class SiteRule {
    static TYPES = {
        BLOCKED: 'BLOCKED',
        RESTRICTED: 'RESTRICTED',
    };
    constructor(domain, type, isEnabled = true) {
        this.domain = domain.toLowerCase().replace(/^www\./, '');
        this.type = type;
        this.isEnabled = isEnabled;
        this.createdAt = Date.now();
    }
    isMatch(urlOrDomain) {
        try {
            let domain;
            if (urlOrDomain.includes('://')) {
                const url = new URL(urlOrDomain);
                domain = url.hostname.toLowerCase().replace(/^www\./, '');
            } else {
                domain = urlOrDomain.toLowerCase().replace(/^www\./, '');
            }
            return this.domain === domain || domain.endsWith('.' + this.domain);
        } catch {
            return false;
        }
    }
}

class RestrictedRule extends SiteRule {
    constructor(domain, timeLimitMinutes = 30, isEnabled = true) {
        super(domain, SiteRule.TYPES.RESTRICTED, isEnabled);
        this.timeLimitMinutes = timeLimitMinutes;
    }
    evaluate(usageStats) {
        if (!this.isEnabled) {
            return { shouldBlock: false, reason: null };
        }
        const todayTimeMinutes = (usageStats?.todayTimeSeconds || 0) / 60;
        const remaining = this.timeLimitMinutes - todayTimeMinutes;
        if (todayTimeMinutes >= this.timeLimitMinutes) {
            return { shouldBlock: true, reason: 'restricted', remainingMinutes: 0 };
        }
        return { shouldBlock: false, reason: null, remainingMinutes: Math.ceil(remaining) };
    }
}

class RuleManager {
    constructor() {
        this.rules = new Map();
    }
    addRule(rule) {
        this.rules.set(rule.domain, rule);
    }
    getRule(domain) {
        const normalizedDomain = domain.toLowerCase().replace(/^www\./, '');
        const direct = this.rules.get(normalizedDomain);
        if (direct) return direct;
        for (const rule of this.rules.values()) {
            if (rule.isMatch(normalizedDomain)) {
                return rule;
            }
        }
        return undefined;
    }
    evaluateAccess(url, usageStats = {}, groupUsageSecondsMap = {}) {
        try {
            const urlObj = new URL(url);
            const domain = urlObj.hostname.toLowerCase().replace(/^www\./, '');

            // 1. Individual rule check
            const rule = this.getRule(domain);
            if (rule && rule.isEnabled) {
                const result = rule.evaluate(usageStats);
                if (result.shouldBlock) {
                    return { ...result, domain };
                }
            }
            return { shouldBlock: false, reason: null, domain };
        } catch (error) {
            console.error('Error evaluating access:', error);
            return { shouldBlock: false, reason: null, domain: '' };
        }
    }
}

function getRealTimeUsage(domain, allUsage, rule, pendingUpdates, currentTrack) {
    let totalSeconds = 0;
    for (const [d, domainUsage] of Object.entries(allUsage)) {
        const matchesRule = rule ? rule.isMatch(d) : d === domain || d.endsWith('.' + domain);
        if (matchesRule) {
            totalSeconds += domainUsage.today || 0;
        }
    }
    return totalSeconds;
}

const ruleManager = new RuleManager();
ruleManager.addRule(new RestrictedRule('yahoo.com', 1));

const allUsage = {
    'ca.yahoo.com': { today: 75 }
};

const domain = 'ca.yahoo.com';
const rule = ruleManager.getRule(domain);
console.log('Rule found for ca.yahoo.com:', rule ? rule.domain : 'none');

const totalSeconds = getRealTimeUsage(domain, allUsage, rule);
console.log('Total seconds for ca.yahoo.com:', totalSeconds);

const accessResult = ruleManager.evaluateAccess('https://ca.yahoo.com/', { todayTimeSeconds: totalSeconds });
console.log('Access result:', accessResult);
