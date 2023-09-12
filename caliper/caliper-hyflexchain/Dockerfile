FROM hyperledger/caliper:0.5.0

USER root

RUN chown -R node:node .

USER node:node

ENV CALIPER_BIND_SUT=hyflexchain:1.0.0

COPY --chown=node:node index.js ./
COPY --chown=node:node package.json ./
COPY --chown=node:node sut.yaml ./

RUN caliper bind --caliper-bind-file sut.yaml

COPY --chown=node:node lib ./lib
COPY --chown=node:node launch-master.sh ./
COPY --chown=node:node run-test.sh run-tests.sh ./

ENV CALIPER_BENCHCONFIG=config/benchmark_install_sc.yaml
# ENV CALIPER_BENCHCONFIG=config/benchmark_sc_piggybacked_pow.yaml
# ENV CALIPER_BENCHCONFIG=config/benchmark_sc_piggybacked_bftsmart.yaml
ENV CALIPER_NETWORKCONFIG=config/networkconfig.json

USER root

ENTRYPOINT [  ]

CMD [ "/bin/sh" ]

# caliper launch manager --caliper-bind-file sut.yaml
