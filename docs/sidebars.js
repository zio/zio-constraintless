const sidebars = {
  sidebar: [
    "index",
    "getting-started",
    "explanation/index",
    "explanation/why-not-the-hughes-schema",
    "reference",
    "tutorial",
    {
      type: "category",
      label: "How-to Guides",
      collapsed: true,
      items: [
        "guides/add-an-interpreter",
        "guides/allow-a-new-type",
        "guides/require-one-list-inside-another",
        "guides/use-on-scala-2-and-3"
      ]
    },
    "resources"
  ]
};

module.exports = sidebars;
