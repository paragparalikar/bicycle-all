package com.bicycle.client.shoonya.api.symbol;

import com.bicycle.client.shoonya.api.model.ShoonyaExchange;
import com.bicycle.client.shoonya.api.model.ShoonyaSymbol;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RequiredArgsConstructor
public class ShoonyaHttpSymbolDataLoader implements ShoonyaSymbolDataLoader {

    @Override
    @SneakyThrows
    @SuppressWarnings("unused") 
    public Collection<ShoonyaSymbol> loadByShoonyaExchange(ShoonyaExchange shoonyaExchange) {
        final URI uri = URI.create("https://api.shoonya.com/" + shoonyaExchange.name() + "_symbols.txt.zip");
        final ZipInputStream zis = new ZipInputStream(uri.toURL().openStream());
        final ZipEntry zipEntry = zis.getNextEntry();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
        return reader.lines()
                .skip(1)
                .map(ShoonyaSymbol::new)
                .distinct()
                .toList();
    }

}
